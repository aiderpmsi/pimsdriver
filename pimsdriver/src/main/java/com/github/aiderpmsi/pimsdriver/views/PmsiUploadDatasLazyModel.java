package com.github.aiderpmsi.pimsdriver.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;

import org.primefaces.model.LazyDataModel;

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
    public List<PmsiUploadElement> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,String> filters) {  
        List<PmsiUploadElement> data = new ArrayList<PmsiUploadElement>(pageSize);  
  
        // Create Query
        StringBuilder query = new StringBuilder("select * from PmsiUpload ");
        
        if (filters.size() != 0 || !generalFilter.equals("all")) {
        	query.append("where ");
            if (generalFilter.equals("notprocessed"))
            	query.append("processed=false ");
            else if (generalFilter.equals("processed"))
            	query.append("processed=true ");
            for (Map.Entry<String, String> filter : filters.entrySet()) {
            	query.append(filter.getKey()).append("=").append(filter.getValue()).append(" ");
            }
        }

        query.append("order by ").append(sortField).append(" ");
        if (sortOrder == SortOrder.ASCENDING)
        	query.append("ASC ");
        else
        	query.append("DESC ");
        
        
        query.append("offset ").append(first).append(" limit ").append(pageSize);

        // EXECUTES THE QUERY
        OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(query.toString());
        ODatabaseDocumentTx tx = DocDbConnectionFactory.getConnection();
        tx.begin();
        List<ODocument> results = tx.command(oquery).execute();
        tx.commit();
        
        // Creates The elements
        for (ODocument result : results) {
        	PmsiUploadElement element = new PmsiUploadElement();
        	element.setRecordId(result.getIdentity());
        	element.setDateEnvoi(result.field(""));
        }
        //filter  
        for(Car car : datasource) {  
            boolean match = true;  
  
            for(Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {  
                try {  
                    String filterProperty = it.next();  
                    String filterValue = filters.get(filterProperty);  
                    String fieldValue = String.valueOf(car.getClass().getField(filterProperty).get(car));  
  
                    if(filterValue == null || fieldValue.startsWith(filterValue)) {  
                        match = true;  
                    }  
                    else {  
                        match = false;  
                        break;  
                    }  
                } catch(Exception e) {  
                    match = false;  
                }   
            }  
  
            if(match) {  
                data.add(car);  
            }  
        }  
  
        //sort  
        if(sortField != null) {  
            Collections.sort(data, new LazySorter(sortField, sortOrder));  
        }  
  
        //rowCount  
        int dataSize = data.size();  
        this.setRowCount(dataSize);  
  
        //paginate  
        if(dataSize > pageSize) {  
            try {  
                return data.subList(first, first + pageSize);  
            }  
            catch(IndexOutOfBoundsException e) {  
                return data.subList(first, first + (dataSize % pageSize));  
            }  
        }  
        else {  
            return data;  
        }  
    }  
}
