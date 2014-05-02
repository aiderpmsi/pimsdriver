package com.github.aiderpmsi.pimsdriver.dao;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.db.DataSourceSingleton;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;

public class NavigationDTO {

	public class YM {
		public Integer year;
		public Integer month;
	}
	
	public class RsfSynthesis {
		public Integer rsfa, rsfb, rsfc, rsfh, rsfi, rsfl, rsfm;
	}

	public class RssSynthesis {
		public Integer main, da, dad, acte, seances;
	}
	
	public class RssContent {
		public HashMap<String, String> rssmain;
		public List<HashMap<String, String>> rssacte = new ArrayList<>();
		public List<HashMap<String, String>> rssda = new ArrayList<>();
		public List<HashMap<String, String>> rssdad = new ArrayList<>();
	}

	public RssContent getRssContent(Long mainId) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			// CREATES THE QUERY
			String query = 
					"WITH RECURSIVE rss AS ( \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, pmel_attributes \n"
					+ "  FROM pmel.pmel_pmsielement WHERE pmel_id = ? \n"
					+ "UNION \n"
					+ "SELECT pmel.pmel_id, pmel.pmel_root, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
					+ "FROM pmel_pmsielement pmel \n"
					+ "JOIN rss rss ON (rss.pmel_id = pmel.pmel_parent) \n"
					+ ") \n"
					+ "SELECT pmel_id, pmel_root, pmel_parent, pmel_type, hstore_to_array(pmel_attributes) FROM rss \n";

			PreparedStatement ps = con.prepareStatement(query);
			ps.setLong(1, mainId);

			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE RSS CONTENT
			RssContent rssContent = new RssContent();
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
				case "rssmain" : rssContent.rssmain = hm; break;
				case "rssacte" : rssContent.rssacte.add(hm); break;
				case "rssda" : rssContent.rssda.add(hm); break;
				case "rssdad" : rssContent.rssdad.add(hm); break;
				}
			}
			
			// COMMIT
			con.commit();
			
			return rssContent;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents finess", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}
	
	public List<String> getFiness(PmsiUploadedElementModel.Status status) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();
			
			// CREATES THE QUERY
			String query = 
					"SELECT DISTINCT(plud_finess) FROM plud_pmsiupload "
					+ "WHERE plud_processed = ?::public.plud_status ORDER BY plud_finess ASC";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, status.toString());

			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE LIST OF ELEMENTS
			List<String> finesses = new ArrayList<>();
			while (rs.next()) {
				finesses.add(rs.getString(1));
			}
			
			// COMMIT
			con.commit();
			
			return finesses;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents finess", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}

	public List<YM> getYM(PmsiUploadedElementModel.Status status, String finess) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();

			// CREATES THE QUERY
			String query = 
					"SELECT DISTINCT ON (plud_year, plud_month) plud_year, plud_month FROM plud_pmsiupload "
					+ "WHERE plud_processed = ?::public.plud_status AND plud_finess = ? ORDER BY plud_year DESC, plud_month DESC";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, status.toString());
			ps.setString(2, finess);
			
			// EXECUTE QUERY
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE LIST OF ELEMENTS
			List<YM> yms = new ArrayList<>();
			while (rs.next()) {
				YM ym = new YM();
				ym.year = rs.getInt(1);
				ym.month = rs.getInt(2);
				yms.add(ym);
			}
			
			// COMMIT
			con.commit();
			
			return yms;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération des différents mois / années du finess", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}

	}
	
	public RsfSynthesis rsfSynthesis(Long uploadedId) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();

			// CREATES THE QUERY
			String query = 
					"WITH RECURSIVE all_lines AS ( \n"
					+ "SELECT pmel_id, pmel_parent, pmel_type, pmel_attributes \n"
					+ "FROM pmel.pmel_" + uploadedId + " WHERE pmel_type = 'rsfheader' \n"
					+ "UNION \n"
					+ "SELECT pmel.pmel_id, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
					+ "FROM pmel.pmel_" + uploadedId + " pmel \n"
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
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE ITEMS
			RsfSynthesis rsfs = new RsfSynthesis();
			rs.next();
			rsfs.rsfa = (int) rs.getLong(1);
			rsfs.rsfb = (int) rs.getLong(2);
			rsfs.rsfc = (int) rs.getLong(3);
			rsfs.rsfh = (int) rs.getLong(4);
			rsfs.rsfi = (int) rs.getLong(5);
			rsfs.rsfl = (int) rs.getLong(6);
			rsfs.rsfm = (int) rs.getLong(7);
			
			// COMMIT
			con.commit();
			
			return rsfs;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération du nombre de lignes dans le rsf", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}

	public RssSynthesis rssSynthesis(Long uploadedId) {
		Connection con = null;
		
		try {
			// GETS THE DB CONNECTION
			con = DataSourceSingleton.getInstance().getConnection();

			// FIRST CHECK THAT ONE RSSHEADER IS AVAILABLE
			String ckeckQuery = "SELECT COUNT(pmel_id) as nbrows FROM pmel.pmel_" + uploadedId + " WHERE pmel_type = 'rssheader'";
			PreparedStatement pscheck = con.prepareStatement(ckeckQuery);
			
			// EXECUTE QUERY
			ResultSet checkrs = pscheck.executeQuery();
			
			checkrs.next();
			if (checkrs.getLong(1) == 0L)
				return null;
			
			// ONE RSS HEADER IS AVAILABLE, COUNT THE NUMBER OF LINES
			// CREATES THE QUERY
			String query = 
					"WITH RECURSIVE all_lines AS ( \n"
					+ "SELECT pmel_id, pmel_parent, pmel_type, pmel_attributes \n"
					+ "FROM pmel.pmel_" + uploadedId + " WHERE pmel_type = 'rssheader' \n"
					+ "UNION \n"
					+ "SELECT pmel.pmel_id, pmel.pmel_parent, pmel.pmel_type, pmel.pmel_attributes \n"
					+ "FROM pmel.pmel_" + uploadedId + " pmel \n"
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
			ResultSet rs = ps.executeQuery();
			
			// FILLS THE ITEMS
			RssSynthesis rsss = new RssSynthesis();
			rs.next();
			rsss.main = (int) rs.getLong(1);
			rsss.acte = (int) rs.getLong(2);
			rsss.da = (int) rs.getLong(3);
			rsss.dad = (int) rs.getLong(4);
			rsss.seances = (int) rs.getLong(5);
			
			// COMMIT
			con.commit();
			
			return rsss;
		} catch (SQLException e) {
			try { con.rollback(); } catch (SQLException e2) { e2.addSuppressed(e); throw new RuntimeException(e2); }
			throw new TransactionException("Erreur de récupération du nombre de lignes dans le rss", e);
		} finally {
			if (con != null)
				try { con.close(); } catch (SQLException e) { throw new RuntimeException(e); }
		}
	}
	
	
}
