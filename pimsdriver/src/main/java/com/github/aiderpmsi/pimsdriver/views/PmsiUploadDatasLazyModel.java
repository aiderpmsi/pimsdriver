package com.github.aiderpmsi.pimsdriver.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class PmsiUploadDatasLazyModel extends
	LazyDataModel<PmsiUploadElement> {

	/**
	 * Generated serial id
	 */
	private static final long serialVersionUID = 7736732108849412677L;
	
	private String generalFilter;

	public PmsiUploadDatasLazyModel(String generalFilter) {
		this.generalFilter = generalFilter;
	}

	@Override
	public List<PmsiUploadElement> load(int first, int pageSize,
			SortCriteria[] sortCrit, Map<String, String> filters) {

		List<PmsiUploadElement> data = new ArrayList<PmsiUploadElement>(pageSize);

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
			} else {
				query.append("1 = 1 ");
				countquery.append("1 = 1 ");
			}
		}
		
		if (filters != null && !filters.isEmpty()) {
			for (Entry<String, String> filter : filters.entrySet()) {
				query.append("AND ").append(filter.getKey()).append(" = ").append(filter.getValue()).append(" ");
				countquery.append("AND ").append(filter.getKey()).append(" = ").append(filter.getValue()).append(" ");
			}
		}

		boolean firstSort = true;
		for (SortCriteria crit : sortCrit) {
			if (!firstSort) {
				query.append(", ");
				firstSort = false;
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			String criteria = (String) crit.getExpression().getValue(facesContext.getELContext());
			String value = crit.getPropertyName();
			query.append(criteria).append(" ").append(value);
		}

		query.append("offset ").append(first).append(" limit ").append(pageSize);

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
		setRowCount(((Long) countresult.get(0).field("count")).intValue());

		// Wrapped datas
		setWrappedData(data);

		return data;
	}

}
