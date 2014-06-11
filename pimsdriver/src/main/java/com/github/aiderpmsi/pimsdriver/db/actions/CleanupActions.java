package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.CleanupDTO;

public class CleanupActions {
	
	public List<Long> getToCleanup() throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection()) {
			CleanupDTO cu = new CleanupDTO(con);
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					List<Long> cleanup = cu.readList();
					// SELECTION HAS SUCCEDDED
					con.commit();
					return cleanup;
				} catch (SQLException e) {
					if (!e.getSQLState().equals("40001")) {
						con.rollback();
						throw e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}

	public void cleanup(Long cleanupId) throws ActionException {

		try (Connection con = DataSourceSingleton.getInstance().getConnection()) {
			CleanupDTO cu = new CleanupDTO(con);
			
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (;;) {
				try {
					cu.delete(cleanupId);
					// SELECTION HAS SUCCEDDED
					con.commit();
					// GO OUT
					return;
				} catch (SQLException e) {
					if (!e.getSQLState().equals("40001")) {
						con.rollback();
						throw e;
					}
				}
			}
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}
}
