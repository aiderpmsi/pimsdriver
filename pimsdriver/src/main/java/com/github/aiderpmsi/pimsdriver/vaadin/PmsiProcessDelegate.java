package com.github.aiderpmsi.pimsdriver.vaadin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.sqlcontainer.RowItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class PmsiProcessDelegate implements QueryDelegate {

	private static final long serialVersionUID = 3686523542320482729L;
	private List<Filter> filters = new ArrayList<>(0);
	private List<OrderBy> orderBys = new ArrayList<>(0);
	
	@Override
	public int getCount() throws SQLException {
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(
					"SELECT COUNT(*) as nbrows FROM PmsiUpload WHERE processed='pending'");
			tx.begin();
			results = tx.command(oquery).execute();
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
			tx = null;
		}
		Integer nbrows = results.get(0).field("nbrows", OType.INTEGER);
		return nbrows;
	}

	@Override
	public ResultSet getResults(int offset, int pagelength) throws SQLException {
		
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		
		// CREATE QUERY
		StringBuilder query = new StringBuilder("SELECT * FROM PmsiUpload WHERE processed='waiting' ");

		// DO NOTHING FOR THE MOMENT WITH FILTERS AND ORDERS
		
		// OFFSET
		query.append("OFFSET ").append(offset).append(" LIMIT ").append(pagelength).append(" ");
		
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(
					query.toString());
			tx.begin();
			results = tx.command(oquery).execute();
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
			tx = null;
		}
		ResultSet res = new Resul
		Integer nbrows = results.get(0).field("nbrows", OType.INTEGER);
		return nbrows;
	}

	@Override
	public boolean implementationRespectsPagingLimits() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFilters(List<Filter> filters)
			throws UnsupportedOperationException {
		this.filters = filters;
	}

	@Override
	public void setOrderBy(List<OrderBy> orderBys)
			throws UnsupportedOperationException {
		this.orderBys = orderBys;
	}

	@Override
	public int storeRow(RowItem row) throws UnsupportedOperationException,
			SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean removeRow(RowItem row) throws UnsupportedOperationException,
			SQLException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void beginTransaction() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getPrimaryKeyColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsRowWithKey(Object... keys) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
