package com.github.aiderpmsi.pimsdriver.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;

public abstract class DbAction {

	protected interface DbExecution <R> {
		
		public R execute(final Connection conn) throws SQLException;
		
	}
	
	public <R> R execute(DbExecution<R> execution) throws ActionException {
		try (Connection con = DataSourceSingleton.getInstance().getConnection()) {
			// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
			for (int i = 0 ; i < 1000 ; i++) {
				try {
					R result = execution.execute(con);
					// SELECTION HAS SUCCEDDED
					con.commit();
					return result;
				} catch (SQLException e) {
					if (e.getSQLState().equals("40001")) {
						con.rollback();
						// ROLLBACK AND RETRY
					} else {
						throw e;
					}
				}
			}
			// IF WE'RE THERE, WE TRIED 1000 TIMES AND IT WAS UNPOSSIBLE TO ACHIEVE THE EXECUTION
			throw new SQLException("More than " + 1000 + " serializations exceptions");
		} catch (SQLException e) {
			throw new ActionException(e);
		}
	}

}
