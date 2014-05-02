package com.github.aiderpmsi.pimsdriver.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.dao.model.UploadedPmsi;
import com.github.aiderpmsi.pimsdriver.dao.model.navigation.RsfOverview;
import com.github.aiderpmsi.pimsdriver.dao.model.navigation.RssContent;
import com.github.aiderpmsi.pimsdriver.dao.model.navigation.RssOverview;
import com.github.aiderpmsi.pimsdriver.dao.model.navigation.YM;

public class NavigationDTO {

	public RssContent readRssContent(Connection con, UploadedPmsi model, Long rssId) throws SQLException {
		// CREATES THE QUERY
		String query = 
				"WITH RECURSIVE rss AS ( \n"
						+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes \n"
					+ "  FROM pmel.pmel_" + model.getRecordid() + " WHERE pmel_id = ? \n"
					+ "UNION \n"
					+ "SELECT pmel.pmel_id, pmel.pmel_root, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
					+ "FROM pmel." + model.getRecordid() + " pmel \n"
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
		String query = 
				"SELECT DISTINCT ON (plud_year, plud_month) plud_year, plud_month FROM plud_pmsiupload "
						+ "WHERE plud_processed = ?::public.plud_status AND plud_finess = ? ORDER BY plud_year DESC, plud_month DESC";
		PreparedStatement ps = con.prepareStatement(query);
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
		} finally {
			if (rs != null) rs.close();
		}
		
		return yms;
	}
	
	public RsfOverview readRsfOverview(Connection con, UploadedPmsi model) throws SQLException {

		// CREATES THE QUERY
		String query = 
				"WITH RECURSIVE all_lines AS ( \n"
						+ "SELECT pmel_id, pmel_parent, pmel_type, pmel_attributes \n"
						+ "FROM pmel.pmel_" + model.getRecordid() + " WHERE pmel_type = 'rsfheader' \n"
						+ "UNION \n"
						+ "SELECT pmel.pmel_id, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
						+ "FROM pmel.pmel_" + model.getRecordid() + " pmel \n"
						+ "JOIN all_lines al \n"
						+ "ON (al.pmel_id = pmel.pmel_parent) \n"
						+ "), \n"
						+ "rsfa AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfa' \n"
						+ "), \n"
						+ "rsfb AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfb' \n"
						+ "), \n"
						+ "rsfc AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfc' \n"
						+ "), \n"
						+ "rsfh AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfh' \n"
						+ "), \n"
						+ "rsfi AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfi' \n"
						+ "), \n"
						+ "rsfl AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfl' \n"
						+ "), \n"
						+ "rsfm AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rsfm' \n"
						+ ") \n"
						+ "SELECT rsfa.nbrows rsfa , rsfb.nbrows rsfb, rsfc.nbrows rsfc, rsfh.nbrows rsfh, rsfi.nbrows rsfi, rsfl.nbrows rsfl, rsfm.nbrows rsfm \n"
						+ "FROM rsfa rsfa \n"
						+ "CROSS JOIN rsfb rsfb \n"
						+ "CROSS JOIN rsfc rsfc \n"
						+ "CROSS JOIN rsfh rsfh \n"
						+ "CROSS JOIN rsfi rsfi \n"
						+ "CROSS JOIN rsfl rsfl \n"
						+ "CROSS JOIN rsfm rsfm";
		PreparedStatement ps = con.prepareStatement(query);
		
		// EXECUTE QUERY
		ResultSet rs = null;
		// OVERVIEW THE ITEMS
		RsfOverview rsfs = new RsfOverview();
		
		try {
			rs = ps.executeQuery();
			
			rs.next();
			rsfs.setRsfa(rs.getLong(1));
			rsfs.setRsfb(rs.getLong(2));
			rsfs.setRsfc(rs.getLong(3));
			rsfs.setRsfh(rs.getLong(4));
			rsfs.setRsfi(rs.getLong(5));
			rsfs.setRsfl(rs.getLong(6));
			rsfs.setRsfm(rs.getLong(7));
		
		} finally {
			if (rs != null) rs.close();
		}
			
		return rsfs;
	}

	public RssOverview readRssOverview(Connection con, UploadedPmsi model) throws SQLException {
		// CREATES THE QUERY
		String query = 
				"WITH RECURSIVE all_lines AS ( \n"
						+ "SELECT pmel_id, pmel_parent, pmel_type, pmel_attributes \n"
						+ "FROM pmel.pmel_" + model.getRecordid() + " WHERE pmel_type = 'rssheader' \n"
						+ "UNION \n"
						+ "SELECT pmel.pmel_id, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
						+ "FROM pmel.pmel_" + model.getRecordid() + " pmel \n"
						+ "JOIN all_lines al \n"
						+ "ON (al.pmel_id = pmel.pmel_parent) \n"
						+ "), \n"
						+ "rssmain AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rssmain' \n"
						+ "), \n"
						+ "rssacte AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rssacte' \n"
						+ "), \n"
						+ "rssda AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rssda' \n"
						+ "), \n"
						+ "rssdad AS ( \n"
						+ "SELECT COUNT(*) AS nbrows FROM all_lines WHERE pmel_type = 'rssdad' \n"
						+ "), \n"
						+ "rssseances AS ( \n"
						+ "SELECT SUM((pmel_attributes -> 'NbSeances')::int) AS nbseances FROM all_lines \n"
						+ ") \n"
						+ "SELECT rssmain.nbrows rssmain , rssacte.nbrows rssacte, rssda.nbrows rssda, rssdad.nbrows rssdad, rssseances.nbseances nbseances \n"
						+ "FROM rssmain rssmain \n"
						+ "CROSS JOIN rssacte rssacte \n"
						+ "CROSS JOIN rssda rssda \n"
						+ "CROSS JOIN rssdad rssdad \n"
						+ "CROSS JOIN rssseances rssseances";
		PreparedStatement ps = con.prepareStatement(query);
			
		// EXECUTE QUERY
		ResultSet rs = null;
		// RSF OVERVIEW MODEL
		RssOverview rsss = new RssOverview();
		try {
			rs = ps.executeQuery();

			rs.next();
			rsss.setMain(rs.getLong(1));
			rsss.setActe(rs.getLong(2));
			rsss.setDa(rs.getLong(3));
			rsss.setDad(rs.getLong(4));
			rsss.setSeances(rs.getLong(5));
		} finally {
			if (rs != null) rs.close();
		}
		
		return rsss;
	}
	
}
