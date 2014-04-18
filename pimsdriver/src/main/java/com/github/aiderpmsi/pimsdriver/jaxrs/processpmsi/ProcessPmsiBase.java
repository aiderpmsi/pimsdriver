package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Path;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Path("/process") 
@PermitAll
public class ProcessPmsiBase {
	
	protected List<UploadedElement> getPendingUploadedElements (
			String query, Object[] arguments) {

		// EXECUTES THE QUERY
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(query.toString());
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute(arguments);
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// LIST OF THE ELEMENTS
		List<UploadedElement> upeltslist = new ArrayList<>();
		for (ODocument result : results) {
			// BEAN FOR THIS ITEM
			UploadedElement element = new UploadedElement();

			// FILLS THE BEAN
			element.setRecordId(result.getIdentity());
			element.setDateEnvoi((Date) result.field("dateenvoi"));
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
	
}