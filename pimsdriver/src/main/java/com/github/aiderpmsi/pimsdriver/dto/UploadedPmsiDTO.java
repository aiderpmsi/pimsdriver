package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pimsdriver.db.vaadin.DBQueryBuilder;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class UploadedPmsiDTO {

	public void delete(Connection con, UploadedPmsi model) throws SQLException {

		// USE THE LARGE OBJECT INTERFACE FOR FILES
		@SuppressWarnings("unchecked")
		Connection conn = ((DelegatingConnection<Connection>) con).getInnermostDelegateInternal();
		LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();

		// DELETE LARGE OBJECT IDS
		if (model.getRsfoid() != null)
			lom.delete(model.getRsfoid());
		if (model.getRssoid() != null) {
			lom.delete(model.getRssoid());
		}
		
		// DELETE ELEMENTS FROM PMSI ELEMENTS IF STATUS IS SUCESSED
		if (model.getProcessed() == UploadedPmsi.Status.successed) {
			String deletePmsiElementQuery =
					"TRUNCATE TABLE pmel.pmel_" + model.getRecordid() + "; \n"
							+ "INSERT INTO pmel.pmel_cleanup (plud_id) VALUES (?);";
			PreparedStatement deletePmsiElementPs = con.prepareStatement(deletePmsiElementQuery);
			deletePmsiElementPs.setLong(1, model.getRecordid());
			deletePmsiElementPs.execute();
		}

		// DELETE ELEMENTS FROM PMSI UPLOAD
		String deletePmsiUploadQuery =
				"DELETE FROM plud_pmsiupload WHERE plud_id =  ?";
		PreparedStatement deletePmsiUploadPs = con.prepareStatement(deletePmsiUploadQuery);
		deletePmsiUploadPs.setLong(1, model.getRecordid());
		deletePmsiUploadPs.execute();
	}

	public List<UploadedPmsi> readList (Connection con,
			List<Filter> filters, List<OrderBy> orders, Integer first, Integer rows) throws SQLException {
		// PREPARE THE QUERY :
		StringBuilder query = new StringBuilder(
				"SELECT plud_id, plud_processed, plud_finess, plud_year, plud_month, "
				+ "plud_dateenvoi, plud_rsf_oid, plud_rss_oid, hstore_to_array(plud_arguments) "
				+ "FROM plud_pmsiupload ");
		
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
		
		// CREATE THE DB STATEMENT	
		PreparedStatement ps = con.prepareStatement(query.toString());
		for (int i = 0 ; i < queryArgs.size() ; i++) {
			ps.setObject(i + 1, queryArgs.get(i));
		}

		// EXECUTE QUERY
		ResultSet rs = null;
		// LIST OF ELEMENTS
		List<UploadedPmsi> upeltslist = new ArrayList<>();
		try {
			rs = ps.executeQuery();
			
			// FILLS THE LIST OF ELEMENTS
			while (rs.next()) {
				// BEAN FOR THIS ITEM
				UploadedPmsi element = new UploadedPmsi();

				// FILLS THE BEAN
				element.setRecordid(rs.getLong(1));
				element.setProcessed(UploadedPmsi.Status.valueOf(rs.getString(2)));
				element.setFiness(rs.getString(3));
				element.setYear(rs.getInt(4));
				element.setMonth(rs.getInt(5));
				element.setDateenvoi(rs.getTimestamp(6));
				element.setRsfoid(rs.getLong(7));
				if (rs.wasNull()) element.setRsfoid(null);
				element.setRssoid(rs.getLong(8));
				if (rs.wasNull()) element.setRssoid(null);
				Object[] array = (Object[]) rs.getArray(9).getArray();
				element.setAttributes(new HashMap<String, String>());
				for (int i = 0 ; i < array.length ; i = i + 2) {
					element.getAttributes().put((String) array[i], (String) array[i + 1]);
				}
				
				// ADDS THE BEAN TO THE ELEMENTS
				upeltslist.add(element);
			}
		} finally {
			if (rs != null) rs.close();
		}

		return upeltslist;
	}

	public long listSize(Connection con, List<Filter> filters) throws SQLException {
		// PREPARE THE QUERY :
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM plud_pmsiupload ");
		
		// PREPARES THE LIST OF ARGUMENTS FOR THIS QUERY
		List<Object> queryArgs = new ArrayList<>();
		// CREATES THE FILTERS, THE ORDERS AND FILLS THE ARGUMENTS
		query.append(DBQueryBuilder.getWhereStringForFilters(filters, queryArgs));
		
		// CREATE THE DB STATEMENT	
		PreparedStatement ps = con.prepareStatement(query.toString());
		for (int i = 0 ; i < queryArgs.size() ; i++) {
			ps.setObject(i + 1, queryArgs.get(i));
		}

		// EXECUTE QUERY
		ResultSet rs = null;
		// RESULT
		Long nbResults;
		
		try {
			rs = ps.executeQuery();
			
			rs.next();
			nbResults = rs.getLong(1);
		} finally {
			if (rs != null) rs.close();
		}
		
		return nbResults;
	}

}
