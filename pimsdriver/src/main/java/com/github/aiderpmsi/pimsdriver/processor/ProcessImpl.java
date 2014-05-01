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

import com.github.aiderpmsi.pims.utils.Parser;
import com.github.aiderpmsi.pimsdriver.dao.TransactionException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.RsfContentHandler;
import com.github.aiderpmsi.pimsdriver.db.RssContentHandler;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.github.aiderpmsi.pimsdriver.pmsi.RecorderErrorHandler;

public class ProcessImpl implements Callable<Boolean> {
	
	private PmsiUploadedElementModel element;
	
	public ProcessImpl(PmsiUploadedElementModel element) {
		this.element = element;
	}

	@Override
	public Boolean call() {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			// USE THE LARGE OBJECT INTERFACE FOR FILES
			@SuppressWarnings("unchecked")
			Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
			
			// FIRST GET ALL FIELDS OF PLUD
			String selectquery = 
					"SELECT plud_processed, plud_finess, plud_year, plud_month, "
					+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid, plud_arguments "
					+ "FROM plud_pmsiupload WHERE plud_id = ?";
			PreparedStatement selectps = con.prepareStatement(selectquery);
			selectps.setLong(1, element.getRecordId());
			
			ResultSet rs = selectps.executeQuery();
			rs.next();

			// CREATE THE PARTITION TO INSERT THE DATAS
			String createPartitionQuery = 
					"CREATE TABLE pmel.pmel_" + element.getRecordId() + " () INHERITS (public.pmel_pmsielement)";
			PreparedStatement createPartitionPs = con.prepareStatement(createPartitionQuery);
			createPartitionPs.execute();
			
			// RSF AND RSS FINESSES
			String rsfFiness = null, rssFiness = null;
			
			// GETS RSF INPUTSTREAM
			Long rsfoid = rs.getLong(6);
			InputStream rsfis = lom.open(rsfoid).getInputStream();
			
			// PROCESS RSF
			{
				ContentHandler rsfch = new RsfContentHandler(con, element.getRecordId());
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
				String realrsffinessquery = "SELECT pmel_attributes -> 'Finess' AS finess FROM pmel.pmel_" + element.getRecordId() + " WHERE pmel_type = 'rsfheader'";
				PreparedStatement realrsffinessps = con.prepareStatement(realrsffinessquery);
				ResultSet realrsffinessrs = realrsffinessps.executeQuery();
				if (!realrsffinessrs.next() || realrsffinessrs.getString(1) == null)
					throw new SAXException("RSF : impossible de trouver le finess dans l'entête");
				rsfFiness = realrsffinessrs.getString(1);
			}
			
			// GETS RSS OID
			Long rssoid = rs.getLong(7);

			// IF RSS IS DEFINED, GET ITS CONTENT AND PROCESS IT
			if (!rs.wasNull()) {
				InputStream rssis = lom.open(rssoid, LargeObjectManager.READ).getInputStream();
			
				ContentHandler rssch = new RssContentHandler(con, element.getRecordId());
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
				String realrssfinessquery = "SELECT pmel_attributes -> 'Finess' AS finess FROM pmel.pmel_" + element.getRecordId() + " WHERE pmel_type = 'rssheader'";
				PreparedStatement realrssfinessps = con.prepareStatement(realrssfinessquery);
				ResultSet realrssfinessrs = realrssfinessps.executeQuery();
				if (!realrssfinessrs.next() || realrssfinessrs.getString(1) == null)
					throw new SAXException("RSS : impossible de trouver le finess dans l'entête");
				rssFiness = realrssfinessrs.getString(1);
			}

			// VERIFY THAT RSF AND RSS FINESS MATCH
			if (rsfFiness != null && rssFiness != null && !rsfFiness.equals(rssFiness))
				throw new IOException("Finess dans RSF et RSS ne correspondent pas");
						
			// UPDATE STATUS AND REAL FINESS
			String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'successed'::plud_status, plud_finess = ? WHERE plud_id = ?";
			PreparedStatement updateps = con.prepareStatement(updatequery);
			updateps.setString(1, rsfFiness);
			updateps.setLong(2, element.getRecordId());
			updateps.execute();

			// CREATE CONSTRAINTS ON PMEL TABLE
			String createPartitionConstraints = 
					"ALTER TABLE pmel.pmel_" + element.getRecordId() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordId() + "_pkey PRIMARY KEY (pmel_id); \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordId() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordId() + "_pmel_parent_fkey FOREIGN KEY (pmel_parent) \n"
					+ "  REFERENCES pmel.pmel_" + element.getRecordId() + " (pmel_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "ALTER TABLE pmel.pmel_" + element.getRecordId() + " \n"
					+ "ADD CONSTRAINT pmel_inherited_" + element.getRecordId() + "_pmel_root_fkey FOREIGN KEY (pmel_root) \n"
					+ "  REFERENCES public.plud_pmsiupload (plud_id) MATCH SIMPLE \n"
					+ "  ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED; \n"
					+ "CREATE INDEX pmel_inherited_" + element.getRecordId() + "pmel_root_idx ON pmel.pmel_" + element.getRecordId() + " USING btree (pmel_root);";
			PreparedStatement createPartitionConstraintsPs = con.prepareStatement(createPartitionConstraints);
			createPartitionConstraintsPs.execute();
			
			// EVERYTHING WENT FINE, COMMIT
			con.commit();
		} catch (IOException | SQLException | SAXException e) {
			try {
				// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
				con.rollback();
				String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'failed'::plud_status, plud_arguments = plud_arguments || hstore(?, ?) WHERE plud_id = ?";
				PreparedStatement updateps = con.prepareStatement(updatequery);
				updateps.setString(1, "error");
				updateps.setString(2, e.getMessage() == null ? e.getClass().toString() : e.getMessage());
				updateps.setLong(3, element.getRecordId());
				updateps.execute();
				con.commit();
			} catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de gestion du fichier pmsi", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}

		return null;
	}

}
