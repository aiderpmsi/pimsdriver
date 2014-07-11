package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.dto.StatementProvider.Entry;

public class CleanupDTO extends AutoCloseableDto<CleanupDTO.Cleanup>{

	public enum Cleanup implements StatementProvider {
		LIST,
		DELETE;

		@Override
		public String getStatement(Entry<?>... entries) throws SQLException {
			switch (this) {
			case LIST:
				return "SELECT plud_id FROM pmel.pmel_cleanup";
			case DELETE:
				if (entries.length == 1
				&& entries[0].object != null
				&& entries[0].object instanceof Long)
					return
							  "DROP TABLE pmgr.pmgr_" + entries[0].object + "; \n"
							+ "DROP TABLE pmel.pmel_" + entries[0].object + "; \n"
							+ "DELETE FROM pmel.pmel_cleanup WHERE plud_id = ?;";
				else
					throw new SQLException("getStatement for DELETE element needs not null Long entry");
			}

			// SHOULD NEVER TOUCH THIS POINT
			throw new RuntimeException("This code should never been reached");
		}
	};
	
	public class PmsiEntry {
		public String lineName;
		public long number;
	}
	
	public CleanupDTO(final Connection con, final ServletContext context) {
		super(con, CleanupDTO.Cleanup.class, context);
	}

	public List<Long> readList() throws SQLException {
		// CREATES THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Cleanup.LIST);

		try (ResultSet rs = ps.executeQuery()) {
			
			// FILLS A LINKED LIST OF PLUD ID HAVING TO BE DELETED
			List<Long> elements = new LinkedList<>();
			while (rs.next()) {
				elements.add(rs.getLong(1));
			}
			return elements;
		}

	}

	public Boolean delete(Long cleanupId) throws SQLException {
		// CREATES THE CLEANUPID ENTRY
		Entry<Long> cleanupEntry = new StatementProvider.Entry<>();
		cleanupEntry.object = cleanupId;
		cleanupEntry.clazz = Long.class;
		
		// CREATES THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Cleanup.DELETE, cleanupEntry);
		ps.setLong(1, cleanupId);
		
		// EXECUTE IT
		return ps.execute();
	}	
}
