package com.github.aiderpmsi.pimsdriver.views;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.ExtendedDataModel;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class PmsiUploadDatasLazyModel extends
		ExtendedDataModel<PmsiUploadElement> implements Serializable {

	/**
	 * Serial id
	 */
	private static final long serialVersionUID = 6974017202125075776L;

	private String generalFilter;

	private ORID currentPk;

	private Map<ORID, PmsiUploadElement> cachedElements = new HashMap<ORID, PmsiUploadElement>();

	private List<ORID> keyList = null;

	private int rowCount;

	public PmsiUploadDatasLazyModel(String generalFilter) {
		this.generalFilter = generalFilter;
	}

	@Override
	public Object getRowKey() {
		return currentPk;
	}

	@Override
	public void setRowKey(Object key) {
		this.currentPk = (ORID) key;
	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public void walk(FacesContext ctx, DataVisitor dv, Range range,
			Object argument) {
		SequenceRange rg = (SequenceRange) range;
		keyList = new ArrayList<ORID>(rg.getRows());

		for (PmsiUploadElement element : getItemsByRange(rg.getFirstRow(),
				rg.getRows())) {
			keyList.add(element.getRecordId());
			cachedElements.put(element.getRecordId(), element);
			dv.process(ctx, element.getRecordId(), argument);
		}

	}

	@Override
	public PmsiUploadElement getRowData() {
		if (currentPk == null) {
			return null;
		} else {
			return cachedElements.get(currentPk);
		}
	}

	private List<PmsiUploadElement> getItemsByRange(int first, int numrows) {

		List<PmsiUploadElement> data = new ArrayList<PmsiUploadElement>(numrows);

		// Create Query
		StringBuilder query = new StringBuilder("select * from PmsiUpload ");
		StringBuilder countquery = new StringBuilder(
				"select count(*) from PmsiUpload ");

		if (!generalFilter.equals("all")) {
			query.append("where ");
			countquery.append("where ");
			if (generalFilter.equals("notprocessed")) {
				query.append("processed=false ");
				countquery.append("processed=false ");
			} else if (generalFilter.equals("processed")) {
				query.append("processed=true ");
				countquery.append("processed=true ");
			}
		}

		query.append("offset ").append(first).append(" limit ").append(numrows);

		// EXECUTES THE QUERY
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(
				query.toString());
		OSQLSynchQuery<ODocument> countoquery = new OSQLSynchQuery<ODocument>(
				countquery.toString());
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		List<ODocument> countresult = null;
		try {
			tx = DocDbConnectionFactory.getConnection();
			tx.begin();
			results = tx.command(oquery).execute();
			countresult = tx.command(countoquery).execute();
			tx.commit();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		} finally {
			if (tx != null)
				tx.close();
		}

		// Creates The elements
		for (ODocument result : results) {
			PmsiUploadElement element = new PmsiUploadElement();
			element.setRecordId(result.getIdentity());
			element.setDateEnvoi(new Date((Long) result.field("dateEnvoi")));
			element.setFinessValue((String) result.field("finessValue"));
			element.setMonthValue((Integer) result.field("monthValue"));
			element.setProcessed((Boolean) result.field("processed"));
			element.setYearValue((Integer) result.field("yearValue"));
			if (result.field("rss") == null)
				element.setComment("RSF");
			else
				element.setComment("RSF et RSS");
			data.add(element);
		}

		// rowCount
		this.rowCount = ((Long) countresult.get(0).field("count")).intValue();

		// Wrapped datas
		this.setWrappedData(data);

		return data;
	}

	@Override
	public boolean isRowAvailable() {
		// Never used
		if (currentPk == null)
			return false;
		else
			return true;
	}

	@Override
	public int getRowIndex() {
		// Unused
		return 0;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		// Unused, ignore
	}

	@Override
	public Object getWrappedData() {
		// Unused
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWrappedData(Object data) {
		// Unused
		throw new UnsupportedOperationException();
	}

}
