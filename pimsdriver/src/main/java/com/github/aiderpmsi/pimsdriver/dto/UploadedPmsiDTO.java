package com.github.aiderpmsi.pimsdriver.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.largeobject.LargeObjectManager;

import com.github.aiderpmsi.pimsdriver.db.vaadin.query.DBQueryBuilder;
import com.github.aiderpmsi.pimsdriver.dto.StatementProvider.Entry;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

public class UploadedPmsiDTO extends AutoCloseableDto<UploadedPmsiDTO.Query> {

	public enum Query implements StatementProvider {
		TRUNCATE_PMEL_PMGR,
		FINISH_CLEANUP;

		@Override
		public String getStatement(Entry<?>... entries) throws SQLException {
			switch (this) {
			case TRUNCATE_PMEL_PMGR:
				if (entries.length == 1
				&& entries[0].object != null
				&& entries[0].object instanceof Long)
					return "TRUNCATE TABLE pmel.pmel_" + entries[0].object + ", pmgr.pmgr_" + entries[0].object;
				else
					throw new SQLException(this.toString() + " needs a not null Long entry");
			case FINISH_CLEANUP:
				return "INSERT INTO pmel.pmel_cleanup (plud_id) VALUES (?);"
				+ "DELETE FROM public.plud_pmsiupload WHERE plud_id =  ?;";
			default: //SHOULD NEVER REACH THIS POINT
				throw new RuntimeException("This code should never been reached");
			}
		}
		
	};

	public UploadedPmsiDTO(final Connection con, final ServletContext context) {
		super(con, UploadedPmsiDTO.Query.class, context);
	}

	public Boolean delete(UploadedPmsi model) throws SQLException {

		// USE THE LARGE OBJECT INTERFACE FOR FILES
		@SuppressWarnings("unchecked")
		Connection conn = ((DelegatingConnection<Connection>) getConnection()).getInnermostDelegateInternal();
		LargeObjectManager lom = ((org.postgresql.PGConnection)conn).getLargeObjectAPI();

		// DELETE LARGE OBJECT IDS
		if (model.rsfoid != null)
			lom.delete(model.rsfoid);
		if (model.rssoid != null) {
			lom.delete(model.rssoid);
		}
		
		// DELETE ELEMENTS FROM PMSI ELEMENTS IF STATUS IS SUCESSED
		if (model.getStatus() == UploadedPmsi.Status.successed) {
			// GET PS
			Entry<Long> id = new Entry<>();
			id.object = model.recordid;
			id.clazz = Long.class;

			// TRUNCATE PMGR AND PMEL TOHETHER (ARE RELATED)
			PreparedStatement ps = getPs(Query.TRUNCATE_PMEL_PMGR, id);
			ps.execute();

		}

		// INSERT INTO PMEL_CLEANUP THAT TWO TABLES HAVE TO BE DROPPED AND REMOVE FROM PLUD THE INSERTION 
		PreparedStatement ps = getPs(Query.FINISH_CLEANUP);
		ps.setLong(1, model.recordid);
		ps.setLong(2, model.recordid);

		ps.execute();
		
		return true;
	}

	public List<UploadedPmsi> readList (List<Filter> filters, List<OrderBy> orders,
			Integer first, Integer rows) throws SQLException {

		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
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
		
		// CREATES THE DB STATEMENT
		try (PreparedStatement ps = getConnection().prepareStatement(query.toString())) {

			for (int i = 0 ; i < queryArgs.size() ; i++) {
				ps.setObject(i + 1, queryArgs.get(i));
			}

			// EXECUTES THE QUERY
			try (ResultSet rs = ps.executeQuery()) {
		
				// LIST OF ELEMENTS
				List<UploadedPmsi> upeltslist = new ArrayList<>();
			
				// FILLS THE LIST OF ELEMENTS
				while (rs.next()) {
					// BEAN FOR THIS ITEM
					UploadedPmsi element = new UploadedPmsi();

					// FILLS THE BEAN
					element.recordid = rs.getLong(1);
					element.status = UploadedPmsi.Status.valueOf(rs.getString(2));
					element.finess = rs.getString(3);
					element.year = rs.getInt(4);
					element.month = rs.getInt(5);
					element.dateenvoi = rs.getTimestamp(6);
					element.rsfoid = rs.getLong(7);
					if (rs.wasNull()) element.rsfoid = null;
					element.rssoid = rs.getLong(8);
					if (rs.wasNull()) element.rssoid = null;
					Object[] array = (Object[]) rs.getArray(9).getArray();
					element.attributes = new HashMap<String, String>();
					for (int i = 0 ; i < array.length ; i = i + 2) {
						element.attributes.put((String) array[i], (String) array[i + 1]);
					}
				
				// ADDS THE BEAN TO THE ELEMENTS
				upeltslist.add(element);

				}
				return upeltslist;
			}
		}
	}

	public long listSize(List<Filter> filters) throws SQLException {
		// IN THIS QUERY, IT IS NOT POSSIBLE TO STORE THE QUERY (CAN CHANGE AT EVERY CALL)
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(*) FROM plud_pmsiupload ");
		
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

}
