package com.github.aiderpmsi.pimsdriver.dto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryBuilder;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfA;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfB;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRsfC;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssActe;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssDa;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssDad;
import com.github.aiderpmsi.pimsdriver.dto.model.BaseRssMain;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class NavigationDTO extends AutoCloseableDto<NavigationDTO.Navigation> {
	
	private static BigDecimal coeff = new BigDecimal(100);
	
	public class YM {
		public Integer year, month;
	}
	
	public class PmsiOverviewEntry {
		public String lineName;
		public long number;
	}
	
	public enum Navigation implements StatementProvider {
		LISTFINESS,
		LISTYM,
		PMSIOVERVIEW,
		RSFASUMMARY,
		RSFBSUMMARY,
		PMSISOURCE,
		RSFCSUMMARY;

		@Override
		public String getStatement(Entry<?>... entries) throws SQLException {
			switch (this) {
			case LISTFINESS:
				return "SELECT DISTINCT(plud_finess) FROM plud_pmsiupload "
				+ "WHERE plud_processed = ?::public.plud_status ORDER BY plud_finess ASC";
			case LISTYM:
				return "SELECT DISTINCT ON (plud_year, plud_month) plud_year, plud_month FROM plud_pmsiupload "
				+ "WHERE plud_processed = ?::public.plud_status AND plud_finess = ? ORDER BY plud_year DESC, plud_month DESC";
			case PMSIOVERVIEW:
				return "WITH RECURSIVE all_lines AS ( \n"
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
			case RSFASUMMARY:
				return "SELECT SUM(cast_to_int(totalfacturehonoraire, NULL)) totalfacturehonoraire, "
				+ "SUM(cast_to_int(totalfactureph, NULL)) totalfactureph "
				+ "FROM fava_rsfa_2012_view WHERE pmel_root = ?";
			case RSFBSUMMARY:
				return "SELECT SUM(cast_to_int(montanttotaldepense, NULL)) montanttotaldepense "
				+ "FROM favb_rsfb_2012_view WHERE pmel_root = ? and pmel_parent = ?";
			case RSFCSUMMARY:
				return "SELECT SUM(cast_to_int(montanttotalhonoraire, NULL)) montanttotalhonoraire "
				+ "FROM favc_rsfc_2012_view WHERE pmel_root = ? and pmel_parent = ?";
			case PMSISOURCE:
				return "WITH RECURSIVE complete AS ( "
						+ "SELECT pmel_root, pmel_line, pmel_position, pmel_content "
						+ "FROM pmel_pmsielement "
						+ "WHERE pmel_root = ? AND pmel_position = ? "
						+ "UNION ALL "
						+ "SELECT pmel.pmel_root, pmel.pmel_line, pmel.pmel_position, pmel.pmel_content "
						+ "FROM pmel_pmsielement pmel "
						+ "JOIN complete cp ON pmel.pmel_root = cp.pmel_root AND pmel.pmel_parent = cp.pmel_position "
						+ ") "
						+ "SELECT string_agg(o.pmel_content, '') "
						+ "FROM (SELECT pmel_line, pmel_content FROM complete ORDER BY pmel_position) o "
						+ "GROUP BY pmel_line "
						+ "ORDER BY pmel_line;";
			default: //SHOULD NEVER REACH THIS POINT
				throw new RuntimeException("This code should never been reached");
			}
		}
	
	};

	public NavigationDTO(final Connection con, final ServletContext context) {
		super(con, NavigationDTO.Navigation.class, context);
	}

	public List<String> readFinessList(UploadedPmsi.Status status) throws SQLException {
		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.LISTFINESS);

		// FILLS THE STATEMENT
		ps.setString(1, status.toString());

		// EXECUTES THE QUERY AND FILLS THE LIST OF FINESSES
		try (ResultSet rs = ps.executeQuery()) {
			
			List<String> finesses = new ArrayList<>();
			while (rs.next()) {
				finesses.add(rs.getString(1));
			}
			return finesses;
		}
	}

	public List<YM> readYMList(UploadedPmsi.Status status,
			String finess) throws SQLException {
		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.LISTYM);

		// FILLS THE STATEMENT
		ps.setString(1, status.toString());
		ps.setString(2, finess);

		// EXECUTES THE QUERY AND FILLS THE LIST OF YM
		try (ResultSet rs = ps.executeQuery()) {
			
			List<YM> yms = new ArrayList<>();
			while (rs.next()) {
				YM ym = new YM();
				ym.year = rs.getInt(1);
				ym.month = rs.getInt(2);
				yms.add(ym);
			}
			return yms;
		}
	}

	public List<PmsiOverviewEntry> readPmsiOverview(UploadedPmsi model, String headerName) throws SQLException {

		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.PMSIOVERVIEW);

		// FILLS THE STATEMENT
		ps.setString(1, headerName);
		ps.setLong(2, model.recordid);
		ps.setLong(3, model.recordid);
		ps.setString(4, headerName);

		// EXECUTES THE QUERY AND FILLS THE OVERVIEW
		try (ResultSet rs = ps.executeQuery()) {
			
			List<PmsiOverviewEntry> lines = new ArrayList<>();
			while (rs.next()) {
				PmsiOverviewEntry entry = new PmsiOverviewEntry();
				entry.lineName = rs.getString(1);
				entry.number = rs.getLong(2);
				lines.add(entry);
			}
			return lines;
		}
	}
	
	public List<BaseRsfA> readRsfAList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT pmel_id, pmel_root, pmel_position, pmel_line, trim(numrss) numrss, "
				+ "trim(sexe) sexe, trim(codess) codess, trim(numfacture) numfacture, "
				+ "cast_to_date(datenaissance, NULL) datenaissance, cast_to_date(dateentree, NULL) dateentree, "
				+ "cast_to_date(datesortie, NULL) datesortie, cast_to_int(totalfacturehonoraire, NULL) totalfacturehonoraire, "
				+ "cast_to_int(totalfactureph, NULL) totalfactureph, trim(etatliquidation) etatliquidation "
				+ "FROM fava_rsfa_2012_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRsfA> rsfa = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRsfA element = new BaseRsfA();

					// FILLS THE BEAN
					element.pmel_id = rs.getLong(1);
					element.pmel_root = rs.getLong(2);
					element.pmel_position = rs.getLong(3);
					element.pmel_line = rs.getLong(4);
					element.numrss = rs.getString(5);
					element.sexe = rs.getString(6);
					element.codess = rs.getString(7);
					element.numfacture = rs.getString(8);
					element.datenaissance = rs.getDate(9);
					element.dateentree = rs.getDate(10);
					element.datesortie = rs.getDate(11);
					element.totalfacturehonoraire = rs.getBigDecimal(12);
					if (element.totalfacturehonoraire != null)
						element.totalfacturehonoraire = element.totalfacturehonoraire.divide(coeff);
					element.totalfactureph = rs.getBigDecimal(13);
					if (element.totalfactureph != null)
						element.totalfactureph = element.totalfactureph.divide(coeff);
					element.etatliquidation = rs.getString(14);
				
				// ADDS THE BEAN TO THE ELEMENTS
				rsfa.add(element);

				}
				return rsfa;
			}
		}
	}

	public BaseRsfA readRsfASummary (Long pmel_root) throws SQLException {
		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.RSFASUMMARY);

		ps.setLong(1, pmel_root);
		
		// EXECUTES THE QUERY
		try (ResultSet rs = ps.executeQuery()) {
		
			if (rs.next()) {
				// ELEMENT TO RETURN
				BaseRsfA rsfa = new BaseRsfA();
				
				rsfa.totalfacturehonoraire = rs.getBigDecimal(1);
				if (rsfa.totalfacturehonoraire != null)
					rsfa.totalfacturehonoraire = rsfa.totalfacturehonoraire.divide(coeff);

				rsfa.totalfactureph = rs.getBigDecimal(2);
				if (rsfa.totalfactureph != null)
					rsfa.totalfactureph = rsfa.totalfactureph.divide(coeff);
				
				return rsfa;
			} else {
				throw new SQLException("Query for root " + pmel_root + " has no row");
			}
		}
	}

	public long readRsfASize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM fava_rsfa_2012_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	public List<BaseRsfB> readRsfBList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT pmel_id, pmel_line, cast_to_date(datedebutsejour, NULL) datedebutsejour, "
				+ "cast_to_date(datefinsejour, NULL) datefinsejour, trim(codeacte) codeacte, "
				+ "cast_to_int(quantite, NULL) quantite, trim(numghs) numghs, cast_to_int(montanttotaldepense, NULL) montanttotaldepense "
				+ "FROM favb_rsfb_2012_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRsfB> rsfbs = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRsfB element = new BaseRsfB();

					// FILLS THE BEAN
					element.pmel_id = rs.getLong(1);
					element.pmel_line = rs.getLong(2);
					element.datedebutsejour = rs.getDate(3);
					element.datefinsejour = rs.getDate(4);
					element.codeacte = rs.getString(5);
					element.quantite = rs.getInt(6);
					element.numghs = rs.getString(7);
					element.montanttotaldepense = rs.getBigDecimal(8);
					if (element.montanttotaldepense != null)
						element.montanttotaldepense = element.montanttotaldepense.divide(coeff);
				
				// ADDS THE BEAN TO THE ELEMENTS
				rsfbs.add(element);

				}
				return rsfbs;
			}
		}
	}

	public BaseRsfB readRsfBSummary (Long pmel_root, Long pmel_position) throws SQLException {
		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.RSFBSUMMARY);

		ps.setLong(1, pmel_root);
		ps.setLong(2, pmel_position);
		
		// EXECUTES THE QUERY
		try (ResultSet rs = ps.executeQuery()) {
		
			if (rs.next()) {
				// ELEMENT TO RETURN
				BaseRsfB rsfb = new BaseRsfB();
				
				rsfb.montanttotaldepense = rs.getBigDecimal(1);
				if (rsfb.montanttotaldepense != null)
					rsfb.montanttotaldepense = rsfb.montanttotaldepense.divide(coeff);
				
				return rsfb;
			} else {
				throw new SQLException("Query for root " + pmel_root + " has no row");
			}
		}
	}

	public long readRsfBSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM favb_rsfb_2012_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	public List<BaseRsfC> readRsfCList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT pmel_id, pmel_line, cast_to_date(dateacte, NULL) dateacte, trim(codeacte) codeacte, "
				+ "cast_to_int(quantite, NULL) quantite, cast_to_int(montanttotalhonoraire, NULL) montanttotalhonoraire "
				+ "FROM favc_rsfc_2012_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRsfC> rsfcs = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRsfC element = new BaseRsfC();

					// FILLS THE BEAN
					element.pmel_id = rs.getLong(1);
					element.pmel_line = rs.getLong(2);
					element.dateacte = rs.getDate(3);
					element.codeacte = rs.getString(4);
					element.quantite = rs.getInt(5);
					element.montanttotalhonoraire = rs.getBigDecimal(6);
					if (element.montanttotalhonoraire != null)
						element.montanttotalhonoraire = element.montanttotalhonoraire.divide(coeff);
				
					// ADDS THE BEAN TO THE ELEMENTS
					rsfcs.add(element);

				}
				return rsfcs;
			}
		}
	}

	public BaseRsfC readRsfCSummary (Long pmel_root, Long pmel_position) throws SQLException {
		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.RSFCSUMMARY);

		ps.setLong(1, pmel_root);
		ps.setLong(2, pmel_position);
		
		// EXECUTES THE QUERY
		try (ResultSet rs = ps.executeQuery()) {
		
			if (rs.next()) {
				// ELEMENT TO RETURN
				BaseRsfC rsfc = new BaseRsfC();
				
				rsfc.montanttotalhonoraire = rs.getBigDecimal(1);
				if (rsfc.montanttotalhonoraire != null)
					rsfc.montanttotalhonoraire = rsfc.montanttotalhonoraire.divide(coeff);
				
				return rsfc;
			} else {
				throw new SQLException("Query for root " + pmel_root + " has no row");
			}
		}
	}

	public long readRsfCSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM favc_rsfc_2012_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	public List<BaseRssMain> readRssMainList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT smva.pmel_id, smva.pmel_position, smva.pmel_line, trim(smva.numrss) numrss, trim(smva.numcmd) || trim(smva.numghm) ghm, "
				+ "pmgr.pmgr_racine || pmgr.pmgr_modalite || pmgr.pmgr_gravite || pmgr.pmgr_erreur ghmcorrige, "
				+ "trim(smva.numlocalsejour) numlocalsejour, trim(smva.numrum) numrum, "
				+ "trim(smva.numunitemedicale) numunitemedicale, cast_to_date(smva.dateentree, NULL) dateentree, "
				+ "cast_to_date(smva.datesortie, NULL) datesortie, cast_to_int(smva.nbseances, NULL) nbseances, "
				+ "trim(smva.dp) dp, trim(smva.dr) dr "
				+ "FROM smva_rssmain_116_view smva "
				+ "JOIN pmgr_pmsigroups pmgr ON "
				+ "    smva.pmel_root = pmgr.pmel_root "
				+ "    AND smva.pmel_id = pmgr.pmel_id ");

		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRssMain> rssmains = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRssMain rssMain = new BaseRssMain();

					// FILLS THE BEAN
					rssMain.pmel_id = rs.getLong(1);
					rssMain.pmel_position = rs.getLong(2);
					rssMain.pmel_line = rs.getLong(3);
					rssMain.numrss = rs.getString(4);
					rssMain.ghm = rs.getString(5);
					rssMain.ghmcorrige = rs.getString(6);
					rssMain.numlocalsejour = rs.getString(7);
					rssMain.numrum = rs.getString(8);
					rssMain.numunitemedicale = rs.getString(9);
					rssMain.dateentree = rs.getDate(10);
					rssMain.datesortie = rs.getDate(11);
					rssMain.nbseances = rs.getInt(12);
					rssMain.dp = rs.getString(13);
					rssMain.dr = rs.getString(14);
					
					rssmains.add(rssMain);
				}
				return rssmains;
			}
		}
	}

	public long readRssMainSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM smva_rssmain_116_view smva "
						+ "JOIN pmgr_pmsigroups pmgr ON "
						+ "smva.pmel_root = pmgr.pmel_root "
						+ "AND smva.pmel_id = pmgr.pmel_id ");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}
	
	public List<BaseRssDa> readRssDaList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT pmel_id, pmel_position, pmel_line, trim(da) "
				+ "FROM sdva_rssda_116_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRssDa> rssdas = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRssDa rssda = new BaseRssDa();

					// FILLS THE BEAN
					rssda.pmel_id = rs.getLong(1);
					rssda.pmel_position = rs.getLong(2);
					rssda.pmel_line = rs.getLong(3);
					rssda.da = rs.getString(4);
					
					rssdas.add(rssda);
				}
				return rssdas;
			}
		}
	}

	public long readRssDaSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM sdva_rssda_116_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}
	
	public List<BaseRssDad> readRssDadList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT pmel_id, pmel_position, pmel_line, trim(dad) "
				+ "FROM sdda_rssdad_116_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRssDad> rssdads = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRssDad rssdad = new BaseRssDad();

					// FILLS THE BEAN
					rssdad.pmel_id = rs.getLong(1);
					rssdad.pmel_position = rs.getLong(2);
					rssdad.pmel_line = rs.getLong(3);
					rssdad.dad = rs.getString(4);
					
					rssdads.add(rssdad);
				}
				return rssdads;
			}
		}
	}

	public long readRssDadSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM sdda_rssdad_116_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}

	
	public List<BaseRssActe> readRssActeList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT pmel_id, pmel_position, pmel_line, cast_to_date(daterealisation, NULL) daterealisation, "
				+ "trim(codeccam) codeccam, trim(phase) phase, trim(activite) activite, cast_to_int(nbacte, NULL) nbacte "
				+ "FROM sava_rssacte_116_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs)).
			append(DBQueryBuilder.getOrderStringForOrderBys(orders, queryArgs));
		// OFFSET AND LIMIT
		if (first != null)
			query.append(" OFFSET ").append(first.toString()).append(" ");
		if (rows != null && rows != 0)
			query.append(" LIMIT ").append(rows.toString()).append(" ");
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<BaseRssActe> rssactes = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					BaseRssActe rssacte = new BaseRssActe();

					// FILLS THE BEAN
					rssacte.pmel_id = rs.getLong(1);
					rssacte.pmel_position = rs.getLong(2);
					rssacte.pmel_line = rs.getLong(3);
					rssacte.daterealisation = rs.getDate(4);
					rssacte.codeccam = rs.getString(5);
					rssacte.phase = rs.getString(6);
					rssacte.activite = rs.getString(7);
					rssacte.nbacte = rs.getInt(8);
					
					rssactes.add(rssacte);
				}
				return rssactes;
			}
		}
	}

	public long readRssActeSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM sava_rssacte_116_view");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {
			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTE QUERY
			try (ResultSet rs = ps.executeQuery()) {

				// RESULT
				rs.next();
				return rs.getLong(1);
			}
		}
	}
	
	public String pmsiSource(long pmel_root, long pmel_position) throws SQLException {
		// GETS THE PREPARED STATEMENT
		PreparedStatement ps = getPs(Navigation.PMSISOURCE);

		ps.setLong(1, pmel_root);
		ps.setLong(2, pmel_position);
		
		// EXECUTES THE QUERY
		try (ResultSet rs = ps.executeQuery()) {
		
			// ELEMENT TO RETURN
			StringBuilder source = new StringBuilder();
			while (rs.next()) {
				
				String current = rs.getString(1); 
				
				if (current != null) {
					source.append(current).append('\n');
				}
				
			}
			return source.toString();
		}
	}

}
