package com.github.aiderpmsi.pimsdriver.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pimsdriver.dao.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.dao.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;

public class ImportPmsiDTO {

	public void importPmsi(UploadPmsi model, InputStream rsf, InputStream rss) throws TransactionException {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			// USE THE LARGE OBJECT INTERFACE FOR FILES
			@SuppressWarnings("unchecked")
			Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
			
			// CREATES AND FILLS THE RSF LARGE OBJECT
			long rsfoid = lom.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
			LargeObject rsfobj = lom.open(rsfoid, LargeObjectManager.WRITE);
			store(rsfobj, rsf);

			// CREATES AND FILLS THE RSS LARGE OBJECT (IF IT EXISTS)
			Long rssoid = null;
			if (rss != null) {
				rssoid = lom.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
				LargeObject rssobj = lom.open(rssoid, LargeObjectManager.WRITE);
				store(rssobj, rss);
			}

			// THEN CREATES THE SQL QUERY TO INSERT EVERYTHING IN PLUD :
			String query = 
					"INSERT INTO plud_pmsiupload("
					+ "plud_processed, plud_finess, plud_year, plud_month, "
					+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid, plud_arguments) "
					+ "VALUES (?::public.plud_status, ?, ?, ?, "
					+ "transaction_timestamp(), ?, ?, hstore(?::text[], ?::text[]));";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, UploadedPmsi.Status.pending.toString());
			ps.setString(2, model.getFiness());
			ps.setInt(3, model.getYear());
			ps.setInt(4, model.getMonth());
			ps.setLong(5, rsfoid);
			if (rssoid != null)
				ps.setLong(6, rssoid);
			else
				ps.setNull(6, Types.BIGINT);
			Array emptyarray = con.createArrayOf("text", new String[]{});
			ps.setArray(7, emptyarray);
			ps.setArray(8, emptyarray);
			
			// EXECUTE QUERY
			ps.execute();
			
			// COMMIT
			con.commit();
		} catch (IOException | SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de stockage de fichier pmsi", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
		
	}
	
	public void deleteUpload(Long uploadId) throws TransactionException {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			// USE THE LARGE OBJECT INTERFACE FOR FILES
			@SuppressWarnings("unchecked")
			Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
			LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();

			// GETS THE RSFOID, RSSOID AND CHECKS THAT THIS UPLOADID EXISTS
			String checkQuery = 
					"SELECT plud_id, plud_rsf_oid, plud_rss_oid, plud_processed FROM plud_pmsiupload "
					+ "WHERE plud_id = ?";
			PreparedStatement checkPs = con.prepareStatement(checkQuery);
			checkPs.setLong(1, uploadId);
			ResultSet checkRs = checkPs.executeQuery();
			
			// IF THERE IS NO RESULT, THIS ID DOESN'T EXISTS
			if (!checkRs.next()) {
				throw new IOException("UploadId " + uploadId + " doesn't exist");
			} else {
				Long rsfoid = checkRs.getLong(2);
				Long rssoid = checkRs.getLong(3);
				if (checkRs.wasNull()) {
					rssoid = null;
				}
				
				// DELETE ELEMENTS FROM PMSI ELEMENTS IF STATUS IS SUCESSED
				if (checkRs.getString(4).equals("successed")) {
					String deletePmsiElementQuery =
							"TRUNCATE TABLE pmel.pmel_" + uploadId + "; \n"
							+ "INSERT INTO pmel.pmel_cleanup (plud_id) VALUES (?);";
					PreparedStatement deletePmsiElementPs = con.prepareStatement(deletePmsiElementQuery);
					deletePmsiElementPs.setLong(1, uploadId);
					deletePmsiElementPs.execute();
				}
				
				// DELETE ELEMENTS FROM LARGE OBJECTS
				lom.delete(rsfoid);
				if (rssoid != null) {
					lom.delete(rssoid);
				}
				
				// DELETE ELEMENTS FROM PMSI UPLOAD
				String deletePmsiUploadQuery =
						"DELETE FROM plud_pmsiupload WHERE plud_id =  ?";
				PreparedStatement deletePmsiUploadPs = con.prepareStatement(deletePmsiUploadQuery);
				deletePmsiUploadPs.setLong(1, uploadId);
				deletePmsiUploadPs.execute();
				
				// VALIDATES THE OPERATIONS
				con.commit();
			}
		} catch (IOException | SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de suppression de fichier pmsi", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
		
	}

	public List<Long> getToCleanup() throws TransactionException {
		Connection con = null;
		
		try {
			// GETS THE DATABASE LINK
			con = DataSourceSingleton.getInstance().getConnection();
			
			// GETS THE UPLOAD ID DELETED
			String checkQuery = 
					"SELECT plud_id FROM pmel.pmel_cleanup";
			PreparedStatement checkPs = con.prepareStatement(checkQuery);
			ResultSet checkRs = checkPs.executeQuery();
			
			// IF THERE IS NO RESULT, THIS ID DOESN'T EXISTS
			List<Long> elements = new LinkedList<>();
			
			while (checkRs.next()) {
				elements.add(checkRs.getLong(1));
			}
				
			// VALIDATES THE OPERATIONS
			con.commit();
			
			return elements;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de sélection de table à nettoyer", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}
	
	public void cleanup(Long pludId) {
		// NOTE THAT WE DON'T CARE IF THIS ID DOESN'T EXIST ANYMORE OR IF WE HAVE AN ERROR :
		// WE WILL NOT TRY AGAIN IF THIS ID DOESN'T EXIST ANYMORE, AND TRY AGAIN IF IT EXISTS
		Connection con = null;
		
		try {
			// GETS THE DB LINK
			con = DataSourceSingleton.getInstance().getConnection();
			
			String deleteTableQuery = "DROP TABLE pmel.pmel_" + pludId + "; \n"
					+ "DELETE FROM pmel.pmel_cleanup WHERE plud_id = ?;";
			PreparedStatement deleteTablePs = con.prepareStatement(deleteTableQuery);
			deleteTablePs.setLong(1, pludId);
			
			deleteTablePs.execute();
			
			con.commit();
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { /* Do Nothing, we should login */ }
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}
	
	private void store(LargeObject lo, InputStream is) throws IOException, SQLException {
		// COPY FROM INPUTSTREAM TO LARGEOBJECT
		byte buf[] = new byte[2048];
		int s = 0;
		while ((s = is.read(buf, 0, 2048)) > 0) {
		   lo.write(buf, 0, s);
		}
	}
}
