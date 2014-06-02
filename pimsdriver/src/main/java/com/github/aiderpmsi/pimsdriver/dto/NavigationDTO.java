package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.aiderpmsi.pims.grouper.model.RssContent;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.PmsiOverviewEntry;
import com.github.aiderpmsi.pimsdriver.dto.model.navigation.YM;

public class NavigationDTO {

	public RssContent readRssContent(Connection con, UploadedPmsi model, Long rssId) throws SQLException {
		// CREATES THE QUERY
		String query = 
				"WITH RECURSIVE rss AS ( \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes \n"
					+ "  FROM pmel.pmel_" + model.getRecordid() + " WHERE pmel_id = ? \n"
					+ "UNION \n"
					+ "SELECT pmel.pmel_id, pmel.pmel_root, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
					+ "FROM pmel.pmel_" + model.getRecordid() + " pmel \n"
					+ "JOIN rss rss ON (rss.pmel_id = pmel.pmel_parent) \n"
					+ ") \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, hstore_to_array(pmel_attributes) FROM rss \n";

		PreparedStatement ps = con.prepareStatement(query);
		ps.setLong(1, rssId);

		// EXECUTE QUERY
		ResultSet rs = null;
		RssContent overview = new RssContent();
		try {
			rs = ps.executeQuery();
			
			while (rs.next()) {
				// CREATE ONE HASHMAP FROM PMEL_ATTRIBUTES
				Array attributes = rs.getArray(5);
				String[] atts = (String[]) attributes.getArray();
				HashMap<String, String> hm = new HashMap<>();
				for (int i = 0 ; i < atts.length ; i = i + 2) {
					hm.put(atts[i], atts[i + 1]);
				}
				String type = rs.getString(4);
				switch(type) {
				case "rssmain" : overview.setRssmain(hm); break;
				case "rssacte" : overview.getRssacte().add(hm); break;
				case "rssda" : overview.getRssda().add(hm); break;
				case "rssdad" : overview.getRssdad().add(hm); break;
				}
			}
		} finally {
			if (rs != null) rs.close();
		}
		
		return overview; 
	}
	
	public List<String> readFinessList(Connection con, UploadedPmsi.Status status) throws SQLException {
		// CREATES THE QUERY
		String query = 
				"SELECT DISTINCT(plud_finess) FROM plud_pmsiupload "
						+ "WHERE plud_processed = ?::public.plud_status ORDER BY plud_finess ASC";
		PreparedStatement ps = con.prepareStatement(query);
		ps.setString(1, status.toString());

		// EXECUTE QUERY
		ResultSet rs = null;
		List<String> finesses = new ArrayList<>();
		try {
			rs = ps.executeQuery();
			
			while (rs.next()) {
				finesses.add(rs.getString(1));
			}
		} finally {
			if (rs != null) rs.close();
		}
		
		return finesses;
	}

	public List<YM> readYMList(Connection con, UploadedPmsi.Status status, String finess) throws SQLException {

		// CREATES THE QUERY
		PreparedStatement ps = null;
		
		try {
			ps = con.prepareStatement(ymQuery);
			ps.setString(1, status.toString());
			ps.setString(2, finess);
			
			// EXECUTE QUERY
			ResultSet rs = null;
			// LIST OF ELEMENTS
			List<YM> yms = new ArrayList<>();
			try {
				rs = ps.executeQuery();
			
				while (rs.next()) {
					YM ym = new YM();
					ym.setYear(rs.getInt(1));
					ym.setMonth(rs.getInt(2));
					yms.add(ym);
				}

				return yms;
			} finally {
				if (rs != null) rs.close();
			}
		} finally {
			if (ps != null) ps.close();
		}
	}
	
	public List<PmsiOverviewEntry> readPmsiOverview(Connection con, UploadedPmsi model, String headerName) throws SQLException {

		// CREATES THE QUERY
		PreparedStatement ps = null;
		
		try {
			ps = con.prepareStatement(nbPmsiLinesQuery);
			ps.setString(1, headerName);
			ps.setLong(2, model.getRecordid());
			ps.setLong(3, model.getRecordid());
			ps.setString(4, headerName);
		
			// EXECUTE QUERY
			ResultSet rs = null;
			// NUMBER OF LINES FOR EACH LINE TYPE
			List<PmsiOverviewEntry> lines = new ArrayList<>();
			try {
				rs = ps.executeQuery();
		
				while (rs.next()) {
					PmsiOverviewEntry entry = new PmsiOverviewEntry();
					entry.lineName = rs.getString(1);
					entry.number = rs.getLong(2);
					lines.add(entry);
				}

				return lines;

			} finally {
				if (rs != null) rs.close();
			}
		} finally {
			if (ps != null)
				ps.close();
		}
	}

	private static final String nbPmsiLinesQuery = 
			"WITH RECURSIVE all_lines AS ( \n"
			+ "SELECT pmel_position, pmel_parent, pmel_type \n"
			+ "FROM public.pmel_pmsielement \n"
			+ "WHERE pmel_type = ? AND pmel_root = ? \n"
			+ "UNION \n"
			+ "SELECT pmel.pmel_position, pmel.pmel_parent, pmel.pmel_type \n"
			+ "FROM public.pmel_pmsielement pmel \n"
			+ "JOIN all_lines al \n"
			+ "ON al.pmel_position = pmel.pmel_parent \n"
			+ "WHERE pmel.pmel_root = ? \n"
			+ ") SELECT pmel_type, COUNT(pmel_type) nb  FROM all_lines \n"
			+ "WHERE pmel_type != ? \n"
			+ "GROUP BY pmel_type \n"
			+ "ORDER BY pmel_type;";

	private static final String ymQuery = 
			"SELECT DISTINCT ON (plud_year, plud_month) plud_year, plud_month FROM plud_pmsiupload "
			+ "WHERE plud_processed = ?::public.plud_status AND plud_finess = ? ORDER BY plud_year DESC, plud_month DESC";
}
