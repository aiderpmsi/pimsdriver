package com.github.aiderpmsi.pimsdriver.db.actions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pims.parser.utils.SimpleParserFactory;
import com.github.aiderpmsi.pims.treebrowser.TreeBrowserException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.GroupHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.RsfLineHandler;
import com.github.aiderpmsi.pimsdriver.db.actions.pmsiprocess.RssLineHandler;
import com.github.aiderpmsi.pimsdriver.dto.ProcessorDTO;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi.Status;

public class ProcessActions extends DbAction {
	
	public ProcessActions(final ServletContext context) {
		super(context);
	}

	public Boolean processPmsi(UploadedPmsi element) throws ActionException {

		// GETS THE DB CONNECTION
		try (Connection con = DataSourceSingleton.getConnection(getServletContext())) {

			// DTO
			try (ProcessorDTO dto = new ProcessorDTO(con, getServletContext())) {

				try {
					// CREATES THE TEMP TABLES
					dto.createTempTables();
					
					// PROCESS PMSIS
					String finess = null, rsfVersion = null, rssVersion = null;
					{
						long pmsiPosition = 0;
						
						SimpleParserFactory pf = new SimpleParserFactory();
		
						// COPY PMSI FILE TO PGSQL
						try (final RsfLineHandler rsfLineHandler = new RsfLineHandler(element.recordid, pmsiPosition)) {
							dto.processPmsi("rsfheader", Arrays.asList(rsfLineHandler), pf, element.rsfoid);
	
							// RETRIEVE ELEMENTS FROM LINE HANDLER
							pmsiPosition = rsfLineHandler.getPmsiPosition();
							finess = rsfLineHandler.getFiness();
							rsfVersion = rsfLineHandler.getVersion();
						}
						
						// PROCESS RSS IF NEEDED
						if (element.rssoid != null) {
						
							// COPY PMSI FILE TO PGSQL
							try (
									final RssLineHandler rssLineHandler = new RssLineHandler(element.recordid, pmsiPosition);
									final GroupHandler groupHandler = new GroupHandler(pmsiPosition);) {
								dto.processPmsi("rssheader", Arrays.asList(rssLineHandler, groupHandler), pf, element.rssoid);
							
								// RETRIEVE ELEEMNTS FROM LINE HANDLER
								pmsiPosition = rssLineHandler.getPmsiPosition();
								rssVersion = rssLineHandler.getVersion();
									
								// CHECK THAT RSFFINESS MATCHES RSSFINESS
								if (!finess.equals(rssLineHandler.getFiness())) {
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
				} catch (IOException | SQLException | TreeBrowserException e) {
					// IF THE EXCEPTION IS DUE TO A SERIALIZATION EXCEPTION, WE HAVE TO RETRY THIS TREATMENT
					if (e instanceof SQLException && ((SQLException) e).getSQLState().equals("40001")) {
						// ROLLBACK, BUT RETRY LATER (DO NOT UPDATE STATUS)
						con.rollback();
						
						return false;
					} else {
						// IF WE HAVE AN ERROR, ROLLBACK TRANSACTION AND STORE THE REASON FOR THE FAILURE
						con.rollback();

						dto.setStatus(element.recordid, Status.failed,
								element.finess, "error", e.getMessage() == null ? e.getClass().toString() : e.getMessage());

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