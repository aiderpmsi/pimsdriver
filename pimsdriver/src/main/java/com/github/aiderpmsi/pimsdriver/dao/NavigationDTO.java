package com.github.aiderpmsi.pimsdriver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.github.aiderpmsi.pimsdriver.odb.DataSourceSingleton;

public class NavigationDTO {

	public class YM {
		public Integer year;
		public Integer month;
	}
	
	public List<String> getFiness(PmsiUploadedElementModel.Status status) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			// CREATES THE QUERY
			String query = 
					"SELECT DISTINCT(plud_finess) FROM plud_pimsupload "
					+ "WHERE processed = ?::public.plud_status ORDER BY plud_finess ASC";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, status.toString());

			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE LIST OF ELEMENTS
			List<String> finesses = new ArrayList<>();
			while (!rs.isLast()) {
				finesses.add(rs.getString(1));
			}
			
			// COMMIT
			con.commit();
			
			return finesses;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents finess", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}

	public List<YM> getYM(String status, String finess) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();

			// CREATES THE QUERY
			String query = 
					"SELECT DISTINCT(plud_year, plud_month) FROM plud_pimsupload "
					+ "WHERE processed = ?::public.plud_status AND finess = ? ORDER BY plud_year DESC, plud_month DESC";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, status.toString());
			ps.setString(2, finess);
			
			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE LIST OF ELEMENTS
			List<YM> yms = new ArrayList<>();
			while (!rs.isLast()) {
				YM ym = new YM();
				ym.year = rs.getInt(1);
				ym.month = rs.getInt(2);
				yms.add(ym);
			}
			
			// COMMIT
			con.commit();
			
			return yms;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents finess", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}

	}
	
	
}
