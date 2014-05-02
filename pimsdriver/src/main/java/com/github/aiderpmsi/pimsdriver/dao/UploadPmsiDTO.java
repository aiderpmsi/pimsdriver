package com.github.aiderpmsi.pimsdriver.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pimsdriver.dao.model.UploadPmsi;
import com.github.aiderpmsi.pimsdriver.dao.model.UploadedPmsi;

public class UploadPmsiDTO {

	public Long create(Connection con, UploadPmsi model, InputStream rsf, InputStream rss) throws SQLException, IOException {
		
		// USE THE LARGE OBJECT INTERFACE FOR FILES
		@SuppressWarnings("unchecked")
		Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
		LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();
			
		// CREATES AND FILLS THE RSF LARGE OBJECT (IF IT EXISTS)
		Long rsfoid = store(lom, rsf), rssoid = store(lom, rss); 

		// THEN CREATES THE SQL QUERY TO INSERT EVERYTHING IN PLUD :
		String query = 
				"INSERT INTO plud_pmsiupload("
						+ "plud_processed, plud_finess, plud_year, plud_month, "
						+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid, plud_arguments) "
						+ "VALUES (?::public.plud_status, ?, ?, ?, "
						+ "transaction_timestamp(), ?, ?, hstore(?::text[], ?::text[])) "
						+ "RETURNING plud_id;";

		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, UploadedPmsi.Status.pending.toString());
		ps.setString(2, model.getFiness());
		ps.setInt(3, model.getYear());
		ps.setInt(4, model.getMonth());
		if (rsfoid != null)
			ps.setLong(6, rsfoid);
		else
			ps.setNull(6, Types.BIGINT);
		if (rssoid != null)
				ps.setLong(6, rssoid);
			else
				ps.setNull(6, Types.BIGINT);
		Array emptyarray = con.createArrayOf("text", new String[]{});
		ps.setArray(7, emptyarray);
		ps.setArray(8, emptyarray);
			
		// EXECUTE QUERY
		ResultSet rs = null;
		Long pludid;
		try {
			rs = ps.executeQuery();
			rs.next();
			pludid = rs.getLong(1);
		} finally {
			if (rs != null) rs.close();
		}
		
		return pludid;
	}

	/**
	 * Stores the inputstream in objectmanager if inputstream is not null
	 * @param lom
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	private Long store(LargeObjectManager lom, InputStream is) throws IOException, SQLException {
		Long oid = null;
	
		if (is != null) {
			// CREATE THE LARGE OBJECT
			oid = lom.createLO();
			LargeObject lo = lom.open(oid, LargeObjectManager.WRITE);

			// COPY FROM INPUTSTREAM TO LARGEOBJECT
			byte buf[] = new byte[2048];
			int s = 0;
			while ((s = is.read(buf, 0, 2048)) > 0) {
				lo.write(buf, 0, s);
			}
		}
			
		return oid;
	}

}
