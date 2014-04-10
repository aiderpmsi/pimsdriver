package com.github.aiderpmsi.pimsdriver.vaadin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.sqlcontainer.RowItem;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class PmsiProcessDelegate implements QueryDelegate {

	@Override
	public int getCount() throws SQLException {
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			StringBuilder query = new StringBuilder("SELECT COUNT(processed) FROM PmsiUpload WHERE processed='pending'");
		} finally {
			if (tx != null)
				tx.close();
			tx = null;
		}
		return 0;
	}

	@Override
	public ResultSet getResults(int offset, int pagelength) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean implementationRespectsPagingLimits() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFilters(List<Filter> filters)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOrderBy(List<OrderBy> orderBys)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub

	}

	@Override
	public int storeRow(RowItem row) throws UnsupportedOperationException,
			SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean removeRow(RowItem row) throws UnsupportedOperationException,
			SQLException {
		// TODO Auto-generated method stub
		return false;
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
