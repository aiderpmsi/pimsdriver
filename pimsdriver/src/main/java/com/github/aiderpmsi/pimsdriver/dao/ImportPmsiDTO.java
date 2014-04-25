package com.github.aiderpmsi.pimsdriver.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pimsdriver.model.PmsiUploadElementModel;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.github.aiderpmsi.pimsdriver.odb.DataSourceSingleton;

public class ImportPmsiDTO {

	public void importPmsi(PmsiUploadElementModel model, InputStream rsf, InputStream rss) throws TransactionException {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			// USE THE LARGE OBJECT INTERFACE FOR FILES
			LargeObjectManager lom = ((org.postgresql.PGConnection)con).getLargeObjectAPI();
			
			// CREATES AND FILLS THE RSF LARGE OBJECT
			long rsfoid = lom.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
			LargeObject rsfobj = lom.open(rsfoid, LargeObjectManager.WRITE);
			store(rsfobj, rsf);

			// CREATES AND FILLS THE RSS LARGE OBJECT (IF IT EXISTS)
			Long rssoid = null;
			if (rss != null) {
				rssoid = lom.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
				LargeObject rssobj = lom.open(rsfoid, LargeObjectManager.WRITE);
				store(rssobj, rss);
			}

			// THEN CREATES THE SQL QUERY TO INSERT EVERYTHING IN PLUD :
			String query = 
					"INSERT INTO plud_pimsupload("
					+ "plud_processed, plud_finess, plud_year, plud_month, "
					+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid, plud_arguments) "
					+ "VALUES (?::public.plud_status, ?, ?, ?, "
					+ "transaction_timestamp(), ?, ?, ?);";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, "pending");
			ps.setString(2, model.getFiness());
			ps.setInt(3, model.getYear());
			ps.setInt(4, model.getMonth());
			ps.setString(5, PmsiUploadedElementModel.Status.pending.toString());
			ps.setLong(6, rsfoid);
			ps.setLong(7, rssoid);
			Array arguments = con.createArrayOf("text", new String[]{});
			ps.setArray(8, arguments);

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

	private void store(LargeObject lo, InputStream is) throws IOException, SQLException {
		// COPY FROM INPUTSTREAM TO LARGEOBJECT
		byte buf[] = new byte[2048];
		int s = 0;
		while ((s = is.read(buf, 0, 2048)) > 0) {
		   lo.write(buf, 0, s);
		}
	}
}
