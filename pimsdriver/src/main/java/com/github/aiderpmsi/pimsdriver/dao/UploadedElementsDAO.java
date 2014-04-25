package com.github.aiderpmsi.pimsdriver.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;
import com.github.aiderpmsi.pimsdriver.odb.DataSourceSingleton;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class UploadedElementsDAO {
	
	public List<PmsiUploadedElementModel> getUploadedElements (
			String query, Object[] arguments) {

		// EXECUTES THE QUERY
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(query.toString());
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DataSourceSingleton.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(arguments);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// LIST OF THE ELEMENTS
		List<PmsiUploadedElementModel> upeltslist = new ArrayList<>();
		for (ODocument result : results) {
			// BEAN FOR THIS ITEM
			PmsiUploadedElementModel element = new PmsiUploadedElementModel();

			// FILLS THE BEAN
			element.setRecordId(result.getIdentity());
			element.setDateenvoi((Date) result.field("dateenvoi"));
			element.setFiness((String) result.field("finess"));
			element.setMonth((Integer) result.field("month"));
			element.setProcessed((String) result.field("processed"));
			element.setYear((Integer) result.field("year"));
			element.setSuccess((Boolean) result.field("success"));
			element.setErrorComment((String) result.field("errorComment"));
			if (result.field("rss") == null)
				element.setComment("RSF seul");
			else
				element.setComment("RSF et RSS");
			upeltslist.add(element);
		}
		
		return upeltslist;
	}
	
	public int size(String query, Object[] arguments) {
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(query);
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DataSourceSingleton.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(arguments);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// GETS THE FIRST RESULT
		return ((Long) results.get(0).field("nbrows")).intValue();
	}

	
}