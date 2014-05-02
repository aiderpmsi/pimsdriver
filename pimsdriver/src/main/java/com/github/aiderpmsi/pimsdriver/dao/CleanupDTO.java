package com.github.aiderpmsi.pimsdriver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class CleanupDTO {

	public List<Long> readList(Connection con) throws SQLException {
		// GETS THE UPLOAD ID DELETED
		String checkQuery = 
				"SELECT plud_id FROM pmel.pmel_cleanup";
		PreparedStatement checkPs = con.prepareStatement(checkQuery);
		ResultSet checkRs = null;

		// FILLS A LINKED LIST
		List<Long> elements = new LinkedList<>();
		try {
			checkRs = checkPs.executeQuery();
			while (checkRs.next()) {
				elements.add(checkRs.getLong(1));
			}
		} finally {
			if (checkRs != null) checkRs.close();
		}
		
		return elements;

	}

	public void delete(Connection con, Long cleanupId) throws SQLException {
		String deleteTableQuery = "DROP TABLE pmel.pmel_" + cleanupId + "; \n"
				+ "DELETE FROM pmel.pmel_cleanup WHERE plud_id = ?;";
		PreparedStatement deleteTablePs = con.prepareStatement(deleteTableQuery);
		deleteTablePs.setLong(1, cleanupId);
				
		deleteTablePs.execute();
	}
	
}
