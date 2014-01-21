package com.github.aiderpmsi.pimsdriver.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class PmsiUploadDatasLazyModel extends LazyDataModel<PmsiUploadElement> {

	/**
	 * Serial id
	 */
	private static final long serialVersionUID = 6974017202125075776L;
	
	private String generalFilter;

	public PmsiUploadDatasLazyModel(String generalFilter) {
		this.generalFilter = generalFilter;
	}
	
    @Override  
    public PmsiUploadElement getRowData(String rowKey) {  
        return null;  
    }  
  
    @Override  
    public Object getRowKey(PmsiUploadElement element) {  
        return element.getRecordId();
    }  
  
	@Override
	public List<PmsiUploadElement> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, String> filters) {
		List<SortMeta> multiSortMeta = new ArrayList<SortMeta>();
		SortMeta sort = new SortMeta();
		sort.setSortField(sortField);
		sort.setSortOrder(sortOrder);
		return load(first, pageSize, multiSortMeta, filters);
	}
	
	@Override
    public List<PmsiUploadElement> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String,String> filters) {

		List<PmsiUploadElement> data = new ArrayList<PmsiUploadElement>(
				pageSize);

		// Create Query
		StringBuilder query = new StringBuilder("select * from PmsiUpload ");
		StringBuilder countquery = new StringBuilder(
				"select count(*) from PmsiUpload ");

		if (filters.size() != 0 || !generalFilter.equals("all")) {
			query.append("where ");
			countquery.append("where ");
			if (generalFilter.equals("notprocessed")) {
				query.append("processed=false ");
				countquery.append("processed=false ");
			} else if (generalFilter.equals("processed")) {
				query.append("processed=true ");
				countquery.append("processed=true ");
			}
			for (Map.Entry<String, String> filter : filters.entrySet()) {
				query.append(filter.getKey()).append("=")
						.append(filter.getValue()).append(" ");
				countquery.append(filter.getKey()).append("=")
						.append(filter.getValue()).append(" ");
			}
		}

		if (multiSortMeta !=null && !multiSortMeta.isEmpty()) {
			query.append("order by ");
			for (SortMeta sort : multiSortMeta) {
				query.append(sort.getSortField()).append(" ");
				if (sort.getSortOrder() == SortOrder.ASCENDING)
					query.append("ASC ");
				else
					query.append("DESC ");
			}
		}

		query.append("offset ").append(first).append(" limit ")
				.append(pageSize);

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
			if (tx != null) tx.close();
		}

		// Creates The elements
		for (ODocument result : results) {
			PmsiUploadElement element = new PmsiUploadElement();
			element.setRecordId(result.getIdentity());
			element.setDateEnvoi(new Date((long) result.field("dateEnvoi")));
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
		this.setRowCount(((Long) countresult.get(0).field("count")).intValue());

		return data;
	}  
}
