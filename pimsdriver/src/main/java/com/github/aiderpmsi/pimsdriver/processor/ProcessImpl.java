package com.github.aiderpmsi.pimsdriver.processor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.parser.utils.Parser2;
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
			Connection innerConn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)innerConn).getLargeObjectAPI();
			
			// CREATE THE TEMP TABLE TO INSERT THE DATAS
			createTempTablePs.execute();
			
			// PROCESS RSF
			PmsiContentHandlerHelper fch = null;
			long pmsiPosition = 0;
			try {
				fch = new RsfContentHandler(con, element.recordid, pmsiPosition);
				processPmsi(fch, "rsfheader", element.rsfoid, lom);
			} finally {
				if (fch != null) {
					fch.close();
					pmsiPosition = fch.getPmsiPosition();
				}
			}

			PmsiContentHandlerHelper rch = null;
			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			if (element.rssoid != null) {
				PreparedStatement createTempGroupTableSt = null;
				try {
					createTempGroupTableSt = con.prepareStatement(createTempGroupTableQuery);
					createTempGroupTableSt.execute();
					try {
						rch = new RssContentHandler(con, element.recordid, pmsiPosition);
						processPmsi(rch, "rssheader", element.rssoid, lom);
					} finally {
						if (rch != null) {
							rch.close();
							pmsiPosition = rch.getPmsiPosition();
						}
					}
				} finally {
					if (createTempGroupTableSt != null) createTempGroupTableSt.close();
				}

				// VERIFY THAT RSF AND RSS FINESS MATCH
				if (!fch.getFiness().equals(rch.getFiness()))
					throw new IOException("Finess dans RSF et RSS ne correspondent pas");
			}

			// CREATE THE PARTITION TO INSERT THE DATAS
			String createPartitionQuery = 
					"CREATE TABLE pmel.pmel_" + element.recordid + " () INHERITS (public.pmel_pmsielement)";
			PreparedStatement createPartitionPs = con.prepareStatement(createPartitionQuery);
			createPartitionPs.execute();

			// COPY TEMP TABLE INTO PMEL TABLE
			String copyQuery = 
					"INSERT INTO pmel.pmel_" + element.recordid + " (pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments) \n"
					+ "SELECT pmel_root, pmel_position, pmel_parent, pmel_type, pmel_line, pmel_content, pmel_arguments FROM pmel_temp";
			PreparedStatement copyPs = con.prepareStatement(copyQuery);
			copyPs.execute();
			
			// CREATE CONSTRAINTS ON PMEL RSF TABLE
			createConstraints(element.recordid, "", con);

			// CREATE GROUP PARTITION
			String createGroupPartitionQuery = 
					"CREATE TABLE pmgr.pmgr_" + element.recordid + " () INHERITS (public.pmgr_pmsigroups)";
			PreparedStatement createGroupPartitionPs = con.prepareStatement(createGroupPartitionQuery);
			createGroupPartitionPs.execute();
			
			if (element.rssoid != null) {
				// COPY ELEMENTS FROM PMGR TEMP TO PMGR DEFINITIVE
				String copyGroupPartitionQuery =
						"INSERT INTO pmgr.pmgr_" + element.recordid + " (pmel_id, pmel_root, pmgr_racine, pmgr_modalite, pmgr_gravite, pmgr_erreur) \n"
						+ "SELECT pmel.pmel_id, " + element.recordid + ", pmgr.pmgr_racine, pmgr.pmgr_modalite, pmgr.pmgr_gravite, pmgr.pmgr_erreur \n"
						+ "FROM pmgr_temp pmgr \n"
						+ "JOIN pmel.pmel_" + element.recordid + " pmel ON \n"
						+ "pmgr.pmel_position = pmel.pmel_position;";
				PreparedStatement copyGroupPartitionPs = con.prepareStatement(copyGroupPartitionQuery);
				copyGroupPartitionPs.execute();
			}

			// CREATE CONSTRAINTS ON PMGR TABLE
			createGroupConstraints(element.recordid, "", con);
			
			// UPDATE STATUS AND REAL FINESS
			StringBuilder updatequery = new StringBuilder("UPDATE plud_pmsiupload SET plud_processed = 'successed'::plud_status, plud_finess = ?, ");
			updatequery.append("plud_arguments = plud_arguments || hstore(?, ?)");
			if (rch != null)
				updatequery.append(" || hstore(?, ?)");
			updatequery.append(" WHERE plud_id = ?");
			PreparedStatement updateps = con.prepareStatement(updatequery.toString());
			int updateindex = 1;
			updateps.setString(updateindex++, fch.getFiness());
			updateps.setString(updateindex++, "rsfversion");
			updateps.setString(updateindex++, fch.getVersion());
			if (rch != null) {
				updateps.setString(updateindex++, "rssversion");
				updateps.setString(updateindex++, rch.getVersion());
			}
			updateps.setLong(updateindex++, element.recordid);
			updateps.execute();
			
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
					updateps.setLong(3, element.recordid);
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
	
	private void createGroupConstraints(long id, CharSequence suffix, Connection con) throws SQLException {
		CharSequence idRepresentation = Long.toString(id);
		CharSequence fs = new StringBuilder(idRepresentation).append(suffix);
		String query = 
				  "ALTER TABLE pmgr.pmgr_" + fs + '\n'
				+ " ADD CONSTRAINT pmgr_inherited_" + fs + "_root_check CHECK (pmel_root = " + id + ") NO INHERIT;\n"
				+ "ALTER TABLE pmgr.pmgr_" + fs + '\n'
				+ " ADD CONSTRAINT pmgr_inherited_" + fs + "_pkey PRIMARY KEY (pmgr_id);\n"
				+ "ALTER TABLE pmgr.pmgr_" + fs + '\n'
				+ " ADD CONSTRAINT pmgr_inherited_" + fs + "_pmel_id_unique UNIQUE (pmel_id);\n"
				+ "ALTER TABLE pmgr.pmgr_" + fs + '\n'
				+ " ADD CONSTRAINT pmgr_inherited_" + fs + "_pmel_root_fkey FOREIGN KEY (pmel_root)\n"
				+ " REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE\n"
				+ " ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n"
				+ "ALTER TABLE pmgr.pmgr_" + fs + '\n'
				+ " ADD CONSTRAINT pmgr_inherited_" + fs + "_pmel_id_fkey FOREIGN KEY (pmel_id)\n"
				+ " REFERENCES pmel.pmel_" + fs + " (pmel_id) MATCH SIMPLE\n"
				+ " ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n";
		con.prepareCall(query).execute();
	}

	private void createConstraints(long id, CharSequence suffix, Connection con) throws SQLException {
		CharSequence idRepresentation = Long.toString(id);
		CharSequence fullSuffix = new StringBuilder(idRepresentation).append(suffix);
		StringBuilder query = new StringBuilder();
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_pkey PRIMARY KEY (pmel_id);\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_line_unique UNIQUE (pmel_position);\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_pmel_parent_fkey FOREIGN KEY (pmel_parent)\n").
			append("REFERENCES pmel.pmel_").append(fullSuffix).append(" (pmel_position) MATCH SIMPLE\n").
			append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_pmel_root_fkey FOREIGN KEY (pmel_root)\n").
			append("REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE\n").
			append("ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED;\n");
		query.append("CREATE INDEX pmel_inherited_").append(fullSuffix).append("_pmel_position_idx ON pmel.pmel_").append(fullSuffix).append(" USING btree (pmel_position);\n");
		query.append("ALTER TABLE pmel.pmel_").append(fullSuffix).append('\n').
			append("ADD CONSTRAINT pmel_inherited_").append(fullSuffix).append("_root_check CHECK (pmel_root = ").append(idRepresentation).append(") NO INHERIT;");
		query.append("CREATE INDEX pmel_inherited_").append(fullSuffix).append("_pmel_rsfb_numrss_idx ON pmel.pmel_").append(fullSuffix).
			append(" (substring(pmel_content from 11 for 20)) WHERE pmel_type = 'rsfb';");
		query.append("CREATE INDEX pmel_inherited_").append(fullSuffix).append("_pmel_rssmain_numrss_idx ON pmel.pmel_").append(fullSuffix).
			append(" (substring(pmel_content from 28 for 20)) WHERE pmel_type = 'rssmain';");
		con.prepareCall(query.toString()).execute();
	}
	
	private void processPmsi(PmsiContentHandlerHelper ch, String pmsitype, Long id, LargeObjectManager lom) throws SQLException, SAXException, IOException, TreeBrowserException {
		InputStream dbFile = null;
		Reader tmpFile = null;
		Path tmpPath = null;
		try {
			dbFile = new BufferedInputStream(lom.open(id).getInputStream());
		
			try {
				tmpPath = Files.createTempFile("", "");
				Files.copy(dbFile, tmpPath, StandardCopyOption.REPLACE_EXISTING);
			} finally {
				dbFile.close();
			}
			
			try {
				tmpFile = Files.newBufferedReader(tmpPath, Charset.forName("ISO-8859-1"));

				RecorderErrorHandler eh = new RecorderErrorHandler();
				Parser2 parser = new Parser2(pmsitype);
				parser.setContentHandler(ch);
				parser.setErrorHandler(eh);
				parser.parse(new InputSource(tmpFile));

				// CHECK IF THERE WERE ANY ERRORS IN PMSI
				if (eh.getErrors().size() != 0)
					throw new SAXException("Processing " + pmsitype + " : error " + eh.getErrors().get(0).getMessage());
				
			} finally {
				if (tmpFile != null) tmpFile.close();
			}
		} finally {
			if (tmpPath != null) Files.delete(tmpPath);
		}

	}
	
	private static final String createTempTableQuery =
			"CREATE TEMPORARY TABLE pmel_temp ( \n"
			+ " pmel_root bigint NOT NULL, \n"
			+ " temp_numrss character varying, \n"
			+ " pmel_position bigint NOT NULL, \n"
			+ " pmel_parent bigint, \n"
			+ " pmel_type character varying NOT NULL, \n"
			+ " pmel_line bigint NOT NULL, \n"
			+ " pmel_content character varying NOT NULL, \n"
			+ " pmel_arguments hstore NOT NULL DEFAULT hstore('')\n"
			+ ") ON COMMIT DROP;";

	private static final String createTempGroupTableQuery =
			"CREATE TEMPORARY TABLE pmgr_temp ( \n"
			+ " pmel_position bigint NOT NULL, \n"
			+ " pmgr_racine character varying NOT NULL, \n"
			+ " pmgr_modalite character varying NOT NULL, \n"
			+ " pmgr_gravite character varying NOT NULL, \n"
			+ " pmgr_erreur character varying NOT NULL \n"
			+ ") ON COMMIT DROP;";
	
}
