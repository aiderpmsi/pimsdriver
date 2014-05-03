package com.github.aiderpmsi.pimsdriver.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.grouper.model.RssContent;
import com.github.aiderpmsi.pims.grouper.utils.Grouper;
import com.github.aiderpmsi.pims.parser.utils.Parser;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.RsfContentHandler;
import com.github.aiderpmsi.pimsdriver.db.RssContentHandler;
import com.github.aiderpmsi.pimsdriver.dto.NavigationDTO;
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
			
			// USE THE LARGE OBJECT INTERFACE FOR FILES
			@SuppressWarnings("unchecked")
			Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
			
			// CREATE THE TEMP TABLE TO INSERT THE DATAS
			String createTempTableQuery =
					"CREATE TEMPORARY TABLE pmel_temp ( \n"
					+ " pmel_id bigint NOT NULL DEFAULT nextval('plud_pmsiupload_plud_id_seq'::regclass), \n"
					+ " pmel_root bigint NOT NULL, \n"
					+ " pmel_parent bigint, \n"
					+ " pmel_type character varying NOT NULL, \n"
					+ " pmel_attributes hstore NOT NULL \n"
					+ ") ON COMMIT DROP";
			PreparedStatement createTempTablePs = con.prepareStatement(createTempTableQuery);
			createTempTablePs.execute();
			
			// RSF AND RSS FINESSES
			String rsfFiness = null, rssFiness = null;
			
			// GETS RSF INPUTSTREAM
			InputStream rsfis = lom.open(element.getRsfoid()).getInputStream();
			
			// PROCESS RSF
			{
				ContentHandler rsfch = new RsfContentHandler(con, element.getRecordid());
				RecorderErrorHandler rsfreh = new RecorderErrorHandler();
				Parser psfpars = new Parser();
				psfpars.setStartState("headerrsf");
				psfpars.setContentHandler(rsfch);
				psfpars.setErrorHandler(rsfreh);
				psfpars.parse(new InputSource(new InputStreamReader(rsfis, "ISO-8859-1")));
				
				// CHECK IF THERE WERE ANY ERRORS IN RSF
				if (rsfreh.getErrors().size() != 0)
					throw new SAXException("RSF : " + rsfreh.getErrors().get(0).getMessage());
					
				// GET THE REAL FINESS FROM RSF
				String realrsffinessquery = "SELECT pmel_attributes -> 'Finess' AS finess FROM pmel_temp WHERE pmel_type = 'rsfheader'";
				PreparedStatement realrsffinessps = con.prepareStatement(realrsffinessquery);
				ResultSet realrsffinessrs = realrsffinessps.executeQuery();
				if (!realrsffinessrs.next() || realrsffinessrs.getString(1) == null)
					throw new SAXException("RSF : impossible de trouver le finess dans l'entête");
				rsfFiness = realrsffinessrs.getString(1);
			}
			
			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			if (element.getRssoid() != null) {
				InputStream rssis = lom.open(element.getRssoid(), LargeObjectManager.READ).getInputStream();
			
				ContentHandler rssch = new RssContentHandler(con, element.getRecordid());
				RecorderErrorHandler rssreh = new RecorderErrorHandler();
				Parser rsspars = new Parser();
				rsspars.setStartState("headerrss");
				rsspars.setContentHandler(rssch);
				rsspars.setErrorHandler(rssreh);
				rsspars.parse(new InputSource(new InputStreamReader(rssis, "ISO-8859-1")));

				// CHECK IF THERE WERE ANY ERRORS IN RSS
				if (rssreh.getErrors().size() != 0)
					throw new SAXException("RSS : " + rssreh.getErrors().get(0).getMessage());
				
				// GET THE REAL FINESS FROM RSS
				String realrssfinessquery = "SELECT pmel_attributes -> 'Finess' AS finess FROM pmel_temp WHERE pmel_type = 'rssheader'";
				PreparedStatement realrssfinessps = con.prepareStatement(realrssfinessquery);
				ResultSet realrssfinessrs = realrssfinessps.executeQuery();
				if (!realrssfinessrs.next() || realrssfinessrs.getString(1) == null)
					throw new SAXException("RSS : impossible de trouver le finess dans l'entête");
				rssFiness = realrssfinessrs.getString(1);
				
				// VERIFY THAT RSF AND RSS FINESS MATCH
				if (!rsfFiness.equals(rssFiness))
					throw new IOException("Finess dans RSF et RSS ne correspondent pas");

				// LISTS EACH PMEL_ID
				String rssMainListQuery = 
						"SELECT pmel_id \n"
						+ "  FROM pmel_temp WHERE pmel_type = 'rssmain'";
				PreparedStatement rssMainListPs = con.prepareStatement(rssMainListQuery);
				ResultSet rssMainListResult = rssMainListPs.executeQuery();
				NavigationDTO nd = new NavigationDTO();

				// GROUPER
				Grouper gp = new Grouper();

				// GROUP EACH MAIN ID
				while (rssMainListResult.next()) {
					// GET THE RSS CONTENT
					// GROUP IT
					RssContent content = nd.readRssContent(con, element, rssMainListResult.getLong(1));
					String result = gp.group(content);
					// UPDATE GROUP IN DB RECORD
					String updateRecordQuery = 
							"UPDATE pmel_temp SET pmel_attributes = pmel_attributes || ('GroupedGHS' => ?) WHERE pmel_id = ?";
					PreparedStatement updateRecordPS = con.prepareStatement(updateRecordQuery);
					updateRecordPS.setString(1, result);
					updateRecordPS.setLong(2, rssMainListResult.getLong(1));
					updateRecordPS.execute();
				}
				
			}

			// CREATE THE PARTITION TO INSERT THE DATAS
			String createPartitionQuery = 
					"CREATE TABLE pmel.pmel_" + element.getRecordid() + " () INHERITS (public.pmel_pmsielement)";
			PreparedStatement createPartitionPs = con.prepareStatement(createPartitionQuery);
			createPartitionPs.execute();
			
			// COPY TEMP TABLE INTO PMEL TABLE
			String copyQuery = 
					"INSERT INTO pmel.pmel_" + element.getRecordid() + " (pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes) \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes FROM pmel_temp";
			PreparedStatement copyPs = con.prepareStatement(copyQuery);
			copyPs.execute();
			
			// UPDATE STATUS AND REAL FINESS
			String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'successed'::plud_status, plud_finess = ? WHERE plud_id = ?";
			PreparedStatement updateps = con.prepareStatement(updatequery);
			updateps.setString(1, rsfFiness);
			updateps.setLong(2, element.getRecordid());
			updateps.execute();

			// CREATE CONSTRAINTS ON PMEL TABLE
			String createPartitionConstraints = 
					"ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pkey PRIMARY KEY (pmel_id); \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pmel_parent_fkey FOREIGN KEY (pmel_parent) \n"
					+ "  REFERENCES pmel.pmel_" + element.getRecordid() + " (pmel_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordid() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordid() + "_pmel_root_fkey FOREIGN KEY (pmel_root) \n"
					+ "  REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "CREATE INDEX pmel_inherited_" + element.getRecordid() + "pmel_root_idx ON pmel.pmel_" + element.getRecordid() + " USING btree (pmel_root);";
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

}
