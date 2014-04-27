package com.github.aiderpmsi.pimsdriver.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.postgresql.largeobject.LargeObjectManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.utils.Parser;
import com.github.aiderpmsi.pimsdriver.dao.TransactionException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.OdbRsfContentHandler;
import com.github.aiderpmsi.pimsdriver.db.OdbRssContentHandler;
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
			LargeObjectManager lom = ((org.postgresql.PGConnection)con).getLargeObjectAPI();
			
			// FIRST GET ALL FIELDS OF PLUD
			String selectquery = 
					"SELECT plud_processed, plud_finess, plud_year, plud_month, "
					+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid, plud_arguments) "
					+ "FROM plud_pmsiupload WHERE plud_id = ?";
			PreparedStatement selectps = con.prepareStatement(selectquery);
			selectps.setLong(1, element.getRecordId());
			
			ResultSet rs = selectps.executeQuery();
			rs.next();

			// RSF AND RSS FINESSES
			String rsfFiness = null, rssFiness = null;
			
			// GETS RSF OID
			Long rsfoid = rs.getLong(6);
			InputStream rsfis = lom.open(rsfoid, LargeObjectManager.READ).getInputStream();

			// PROCESS RSF
			{
				ContentHandler rsfch = new OdbRsfContentHandler(con, element.getRecordId());
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
				String realrsffinessquery = "SELECT pims_attributes->finess AS finess FROM pmel_pmsi_element WHERE pmel_root = ? AND pmel_type = 'rsfheader'";
				PreparedStatement realrsffinessps = con.prepareStatement(realrsffinessquery);
				realrsffinessps.setLong(1, element.getRecordId());
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
			
				ContentHandler rssch = new OdbRssContentHandler(con, element.getRecordId());
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
				String realrssfinessquery = "SELECT pims_attributes->finess AS finess FROM pmel_pmsi_element WHERE pmel_root = ? AND pmel_type = 'rssheader'";
				PreparedStatement realrssfinessps = con.prepareStatement(realrssfinessquery);
				realrssfinessps.setLong(1, element.getRecordId());
				ResultSet realrssfinessrs = realrssfinessps.executeQuery();
				if (!realrssfinessrs.next() || realrssfinessrs.getString(1) == null)
					throw new SAXException("RSS : impossible de trouver le finess dans l'entête");
				rssFiness = realrssfinessrs.getString(1);
			}

			// VERIFY THAT RSF AND RSS FINESS MATCH
			if (rsfFiness != null && rssFiness != null && rsfFiness != rssFiness)
				throw new IOException("Finess dans RSF et RSS ne correspondent pas");
						
			// UPDATE STATUS AND REAL FINESS
			String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'processed', plud_finess = ? WHERE plud_id = ?";
			PreparedStatement updateps = con.prepareStatement(updatequery);
			updateps.setString(1, rsfFiness);
			updateps.setLong(2, element.getRecordId());
			updateps.execute();
			
			// EVERYTHING WENT FINE, COMMIT
			con.commit();
		} catch (IOException | SQLException | SAXException e) {
			try {
				// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
				con.rollback();
				String updatequery = "UPDATE plud_pmsiupload SET plud_processed = 'failed', plud_arguments = plud_arguments || (?, ?) WHERE plud_id = ?";
				PreparedStatement updateps = con.prepareStatement(updatequery);
				updateps.setString(1, "error");
				updateps.setString(2, e.getMessage());
				updateps.execute();
			} catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de gestion du fichier pmsi", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}

		return null;
	}

}
