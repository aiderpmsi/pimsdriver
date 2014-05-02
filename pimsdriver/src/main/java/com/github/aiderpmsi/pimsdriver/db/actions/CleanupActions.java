package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.CleanupDTO;

public class CleanupActions {

	public List<Long> getToCleanup() throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			CleanupDTO cu = new CleanupDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			List<Long> cleanup = null;
			while (cleanup == null) {
				try {
					cleanup = cu.readList(con);
					// SELECTION HAS SUCCEDDED
					con.commit();
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						cleanup = null;
					else
						throw e;
				}
			}
			
			return cleanup;
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}

	public void cleanup(Long cleanupId) throws ActionException {

		Connection con = null;
		
		try {
			con = DataSourceSingleton.getInstance().getConnection();
			CleanupDTO cu = new CleanupDTO();
			
			// STAYS NULL IF SELECTION IS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			Boolean pending = true;
			while (pending == true) {
				try {
					cu.delete(con, cleanupId);
					// SELECTION HAS SUCCEDDED
					con.commit();
					// GO OUT
					pending = false;
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001"))
						pending = true;
					else
						throw e;
				}
			}
			
		} catch (SQLException e) {
			// ERROR : ROLLBACK
			try {if (con != null) con.rollback();} catch (SQLException e2) {
				e2.addSuppressed(e);
				throw new ActionException(e2);
			}
			throw new ActionException(e);
		} finally {
			try {if (con != null) con.close();} catch (SQLException e) {
				throw new ActionException(e);
			}
		}
	}
}
