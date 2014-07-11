package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;

import com.github.aiderpmsi.pimsdriver.dto.StatementProvider.Entry;
import com.vaadin.server.VaadinRequest;

public abstract class AutoCloseableDto<T extends Enum<T> & StatementProvider> implements AutoCloseable {

	private final Connection con;
	
	private final EnumMap<T, PreparedStatement> preparedStatements;
	
	private final VaadinRequest request;

	public AutoCloseableDto(final Connection con, Class<T> clazz, final VaadinRequest request) {
		this.con = con;
		this.preparedStatements = new EnumMap<T, PreparedStatement>(clazz);
		this.request = request;
	}
	
	public PreparedStatement getPs(final T type, final Entry<?>... entries) throws SQLException {
		final PreparedStatement ps;
		if (!preparedStatements.containsKey(type)) {
			ps = con.prepareStatement(type.getStatement(entries));
			preparedStatements.put(type, ps);
		} else {
			ps = preparedStatements.get(type);
		}
		return ps;
	}
	
	public VaadinRequest getRequest() {
		return request;
	}
	
	public Connection getConnection() {
		return con;
	}
	
	@Override
	public void close() throws SQLException {
		// WILL STORE THE DIFFERENT EXCEPTIONS RETURNED IF CLOSE FAILED MULTIPLE TIMES
		SQLException ex = null;
		for (final PreparedStatement ps : preparedStatements.values()) {
			try {
				ps.close();
			} catch (SQLException e) {
				if (ex != null)
					e.addSuppressed(ex);
				ex = e;
			}
		}
		
		if (ex != null)
			throw ex; 
	}

}
