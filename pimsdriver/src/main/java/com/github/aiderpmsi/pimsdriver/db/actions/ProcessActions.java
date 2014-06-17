package com.github.aiderpmsi.pimsdriver.db.actions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.github.aiderpmsi.pims.parser.utils.ParserFactory;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.RsfContentHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.RssContentHandler;
import com.github.aiderpmsi.pimsdriver.dto.ProcessorDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi.Status;

public class ProcessActions {

	public boolean processPmsi(UploadedPmsi element) throws ActionException {

		// GETS THE DB CONNECTION
		try (Connection con = DataSourceSingleton.getInstance().getConnection()) {

			// DTO
			try (ProcessorDTO dto = new ProcessorDTO(con)) {

				try {
					// CREATES THE TEMP TABLES
					dto.createTempTables();
					
					// PROCESS PMSIS
					String finess = null, rsfVersion = null, rssVersion = null;
					{
						long pmsiPosition = 0;
						
						ParserFactory pf = new ParserFactory();
		
						// PROCESS RSF
						try (RsfContentHandler ch = new RsfContentHandler(con, element.recordid, pmsiPosition)) {
							dto.processPmsi("rsfheader", ch, pf, element.rsfoid);
							pmsiPosition = ch.getPmsiPosition();
							finess = ch.getFiness();
							rsfVersion = ch.getVersion();
						}
						
						// PROCESS RSS IF NEEDED
						if (element.rssoid != null) {
							try (RssContentHandler ch = new RssContentHandler(con, element.recordid, pmsiPosition)) {
								dto.processPmsi("rssheader", ch, pf, element.rssoid);
								pmsiPosition = ch.getPmsiPosition();
								rssVersion = ch.getVersion();
								
								// CHECK THAT RSFFINESS MATCHES RSSFINESS
								if (!finess.equals(ch.getFiness())) {
									throw new IOException("Finess dans RSF et RSS ne correspondent pas");
								}
							}
						}
					}
		
					// CREATE THE PARTITION TABLES TO INSERT TEMP DATAS
					dto.createDefinitiveTables(element.recordid);
		
					// COPY TEMP TABLES INTO DEFINITIVE TABLES
					dto.copyTempTables(element.recordid);
					
					// CREATE CONSTRAINTS ON PMEL AND PMGR TABLES
					dto.createContraints(element.recordid);
		
					// UPDATE STATUS AND REAL FINESS
					List<String> parameters = new ArrayList<>();
					parameters.add("rsfversion");parameters.add(rsfVersion);
					if (rssVersion != null) { parameters.add("rssversion");parameters.add(rssVersion); }
					dto.setStatus(element.recordid, Status.successed, finess, parameters.toArray());
					
					// EVERYTHING WENT FINE, COMMIT
					con.commit();
				} catch (IOException | SQLException | SAXException | TreeBrowserException e) {
					// IF THE EXCEPTION IS DUE TO A SERIALIZATION EXCEPTION, WE HAVE TO RETRY THIS TREATMENT
					if (e instanceof SQLException && ((SQLException) e).getSQLState().equals("40001")) {
						// ROLLBACK, BUT RETRY LATER (DO NOT UPDATE STATUS)
						con.rollback();
						
						return false;
					} else {
						// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
						con.rollback();
					
						dto.setStatus(element.recordid, Status.failed, element.finess, "error", e.getMessage() == null ? e.getClass().toString() : e.getMessage());

						con.commit();
						
						return false;
					}
				}
			}
		} catch (SQLException e) {
			if (e.getSQLState().equals("40001")) {
				// IF SERIALISATION EXCEPTION, TRY LATER
			} else {
				// ANY OTHER AEXCEPTION HAS TO BE THROWN
				throw new ActionException(e);
			}
		}
		return true;
	}

}
