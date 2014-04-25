package com.github.aiderpmsi.pimsdriver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.github.aiderpmsi.pimsdriver.odb.DataSourceSingleton;

public class UploadedElementsDAO {
	
	public List<PmsiUploadedElementModel> getUploadedElements (
			String query, Object[] arguments) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			PreparedStatement ps = con.prepareStatement(query);
			for (int i = 0 ; i < arguments.length ; i++) {
				ps.setObject(i + 1, arguments[i]);
			}

			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE LIST OF ELEMENTS
			List<PmsiUploadedElementModel> upeltslist = new ArrayList<>();
			while (!rs.isLast()) {
				// BEAN FOR THIS ITEM
				PmsiUploadedElementModel element = new PmsiUploadedElementModel();

				// FILLS THE BEAN
				element.setRecordId(rs.getLong(1));
				element.setProcessed(PmsiUploadedElementModel.Status.valueOf(rs.getString(2)));
				element.setFiness(rs.getString(3));
				element.setYear(rs.getInt(4));
				element.setMonth(rs.getInt(5));
				element.setDateenvoi(rs.getDate(6));

				// ADDS THE BEAN TO THE ELEMENTS
				upeltslist.add(element);
			}
			
			// COMMIT
			con.commit();
			
			return upeltslist;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents éléménets uploadés", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}
	
	public long size(String query, Object[] arguments) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			PreparedStatement ps = con.prepareStatement(query);
			for (int i = 0 ; i < arguments.length ; i++) {
				ps.setObject(i + 1, arguments[i]);
			}

			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// GETS THE FIRST RESULT
			long nbresults = rs.getLong(1);

			// COMMIT
			con.commit();
			
			return nbresults;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents éléménets uploadés", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}

	
}