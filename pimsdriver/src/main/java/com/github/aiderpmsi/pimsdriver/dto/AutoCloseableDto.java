package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;

import com.github.aiderpmsi.pimsdriver.dto.StatementProvider.Entry;

public abstract class AutoCloseableDto<T extends Enum<T> & StatementProvider> implements AutoCloseable {

	protected Connection con;
	
	protected EnumMap<T, PreparedStatement> preparedStatements;
	
	@SuppressWarnings("unused")
	private AutoCloseableDto() {
		// DO NOT USE
	}

	public AutoCloseableDto(Connection con, Class<T> clazz) {
		this.con = con;
		this.preparedStatements = new EnumMap<T, PreparedStatement>(clazz);
	}
	
	public PreparedStatement getPs(T type, Entry<?>... entries) throws SQLException {
		PreparedStatement ps;
		if (!preparedStatements.containsKey(type)) {
			ps = con.prepareStatement(type.getStatement(entries));
			preparedStatements.put(type, ps);
		} else {
			ps = preparedStatements.get(type);
		}
		return ps;
	}
	
	@Override
	public void close() throws SQLException {
		// WILL STORE THE DIFFERENT EXCEPTIONS RETURNED IF CLOSE FAILED MULTIPLE TIMES
		SQLException ex = null;
		for (PreparedStatement ps : preparedStatements.values()) {
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
