package com.github.aiderpmsi.pimsdriver.db.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.dto.AutoCloseableDto;

public abstract class DbAction {

	private final int MAXRETRYS = 1000;
	
	private final ServletContext context;
	
	public DbAction(final ServletContext context) {
		this.context = context;
	}

	public ServletContext getServletContext() {
		return context;
	}

	@FunctionalInterface
	protected interface DbExecution <R, S extends AutoCloseableDto<?>> {
		public R execute(final S dto) throws SQLException;
	}
	
	public <R, S extends AutoCloseableDto<?>> R execute(final Class<S> dtoClass, final DbExecution<R, S> execution) throws ActionException {
		try {
			final Constructor<S> constructor = dtoClass.getConstructor(Connection.class, ServletContext.class);
			try (
					final Connection con = DataSourceSingleton.getConnection(getServletContext());
					final S dto = constructor.newInstance(con, context);) {
	
				// CONTINUE WHILE SELECTION HAS NOT SUCCEDED BECAUSE OF SERIALIZATION EXCEPTIONS
				for (int i = 0 ; i < MAXRETRYS ; i++) {
					try {
						R result = execution.execute(dto);
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
				throw new SQLException("More than " + MAXRETRYS + " serializations exceptions");
			} catch (SQLException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ActionException(e);
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ActionException(e);
		}
	}

}
