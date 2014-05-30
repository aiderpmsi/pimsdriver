package com.github.aiderpmsi.pimsdriver.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.parser.utils.Parser;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.PmsiContentHandlerHelper;
import com.github.aiderpmsi.pimsdriver.db.RsfContentHandler;
import com.github.aiderpmsi.pimsdriver.db.RssContentHandler;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.pmsi.RecorderErrorHandler;

public class ProcessImpl implements Callable<Boolean> {
	
	private UploadedPmsi element;
	
	public ProcessImpl(UploadedPmsi element) {
		this.element = element;
	}

	@Override
	public Boolean call() throws Exception {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();

			// CREATES THE REUSABLE QUERIES
			PreparedStatement createTempTablePs = con.prepareStatement(createTempTableQuery); // CREATE THE TEMP TABLE TO INSERT THE DATAS
			
			// GETS THE LARGE OBJECT INTERFACE FOR FILES
			@SuppressWarnings("unchecked")
			Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
			
			// CREATE THE TEMP TABLE TO INSERT THE DATAS
			createTempTablePs.execute();
			
			// PROCESS RSF
			PmsiContentHandlerHelper fch = null;
			try {
				fch = new RsfContentHandler(con, element.getRecordid());
				processPmsi(fch, "rsfheader", element.getRsfoid(), lom);
			} finally {
				if (fch != null) fch.close();
			}

			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			if (element.getRssoid() != null) {
				PmsiContentHandlerHelper rch = null;
				try {
					rch = new RssContentHandler(con, element.getRecordid());
					processPmsi(rch, "rssheader", element.getRssoid(), lom);
				} finally {
					if (rch != null) rch.close();
				}

				// VERIFY THAT RSF AND RSS FINESS MATCH
				if (!fch.getFiness().equals(rch.getFiness()))
					throw new IOException("Finess dans RSF et RSS ne correspondent pas");
			}

			// CREATE THE PARTITION TO INSERT THE DATAS
			String createPartitionQuery = 
					"CREATE TABLE pmel.pmel_" + element.getRecordid() + " () INHERITS (public.pmel_pmsielement)";
			PreparedStatement createPartitionPs = con.prepareStatement(createPartitionQuery);
			createPartitionPs.execute();

			// COPY TEMP TABLE INTO PMEL TABLE
			String copyQuery = 
					"INSERT INTO pmel.pmel_" + element.getRecordid() + " (pmel_id, pmel_root, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments) \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments FROM pmel_temp";
			PreparedStatement copyPs = con.prepareStatement(copyQuery);
			copyPs.execute();
			
			// UPDATE STATUS AND REAL FINESS
			String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'successed'::plud_status, plud_finess = ? WHERE plud_id = ?";
			PreparedStatement updateps = con.prepareStatement(updatequery);
			updateps.setString(1, fch.getFiness());
			updateps.setLong(2, element.getRecordid());
			updateps.execute();

			// CREATE CONSTRAINTS ON PMEL TABLE
			String createPartitionConstraints = 
					"ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pkey PRIMARY KEY (pmel_id); \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pmel_parent_fkey FOREIGN KEY (pmel_parent) \n"
					+ "  REFERENCES pmel.pmel_" + element.getRecordid() + " (pmel_line) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pmel_root_fkey FOREIGN KEY (pmel_root) \n"
					+ "  REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "CREATE INDEX pmel_inherited_" + element.getRecordid() + "pmel_line_idx ON pmel.pmel_" + element.getRecordid() + " USING btree (pmel_line);\n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " ADD CHECK (pmel_root = " + element.getRecordid() + ") NO INHERIT;";
			PreparedStatement createPartitionConstraintsPs = con.prepareStatement(createPartitionConstraints);
			createPartitionConstraintsPs.execute();
			
			// EVERYTHING WENT FINE, COMMIT
			con.commit();
		} catch (IOException | SQLException | SAXException e) {
			// IF THE EXCEPTION IS DUE TO A SERIALIZATION EXCEPTION, WE HAVE TO RETRY THIS TREATMENT
			if (e instanceof SQLException && ((SQLException) e).getSQLState().equals("40001")) {
				// ROLLBACK, BUT RETRY LATER
				try {con.rollback();} catch (SQLException e2) { throw new RuntimeException(e); }
				return false;
			} else {
				try {
					// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
					con.rollback();
					String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'failed'::plud_status, plud_arguments = plud_arguments || hstore(?, ?) WHERE plud_id = ?";
					PreparedStatement updateps = con.prepareStatement(updatequery);
					updateps.setString(1, "error");
					updateps.setString(2, e.getMessage() == null ? e.getClass().toString() : e.getMessage());
					updateps.setLong(3, element.getRecordid());
					updateps.execute();
					con.commit();
				} catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
					throw new Exception("Erreur de gestion du fichier pmsi", e);
			}
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}

		return true;
	}

	private void processPmsi(PmsiContentHandlerHelper ch, String pmsitype, Long id, LargeObjectManager lom) throws SQLException, SAXException, IOException, TreeBrowserException {
		InputStream dbFile = null, tmpFile = null;
		Path tmpPath = null;
		try {
			dbFile = lom.open(id).getInputStream();
			try {
				tmpPath = Files.createTempFile("", "");
				Files.copy(dbFile, tmpPath);
				try {
					tmpFile = Files.newInputStream(tmpPath);

					RecorderErrorHandler eh = new RecorderErrorHandler();
					Parser parser = new Parser();
					parser.setType(pmsitype);
					parser.setContentHandler(ch);
					parser.setErrorHandler(eh);
					parser.parse(new InputSource(new InputStreamReader(tmpFile, "ISO-8859-1")));

					// CHECK IF THERE WERE ANY ERRORS IN PMSI
					if (eh.getErrors().size() != 0)
						throw new SAXException("Processing " + pmsitype + " : error " + eh.getErrors().get(0).getMessage());
					
				} finally {
					if (tmpFile != null) tmpFile.close();
				}
			} finally {
				if (tmpPath != null) Files.delete(tmpPath);
			}
		} finally {
			if (dbFile != null) dbFile.close();
		}

	}
	
	private static final String createTempTableQuery =
			"CREATE TEMPORARY TABLE pmel_temp ( \n"
			+ " pmel_id bigint NOT NULL DEFAULT nextval('plud_pmsiupload_plud_id_seq'::regclass), \n"
			+ " pmel_root bigint NOT NULL, \n"
			+ " pmel_parent bigint, \n"
			+ " pmel_type character varying NOT NULL, \n"
			+ " pmel_line bigint NOT NULL, \n"
			+ " pmel_content character varying NOT NULL, \n"
			+ " pmel_arguments hstore NOT NULL DEFAULT hstore('')\n"
			+ ") ON COMMIT DROP;";

}
