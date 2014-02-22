package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Path("/process") 
@PermitAll
public class ProcessPmsi {
	
	@SuppressWarnings("serial")
	public static final Map<Integer, String> orderindex = new HashMap<Integer, String>(){{
		put(1, "dateenvoi");
		put(2, "month");
		put(3, "year");
		put(4, "finess");
		put(5, "processed");
	}};

	@GET
    @Path("/list")
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/processlist.xslt\"?>")
    @Produces({MediaType.APPLICATION_XML})
	public UploadedElements getElements(
			@DefaultValue("0") @QueryParam("first") Integer first,
			@DefaultValue("20") @QueryParam("rows") Integer rows,
			@QueryParam("orderelts") List<String> orderelts,
			@QueryParam("order") List<Boolean> order,
			@DefaultValue("false") @QueryParam("all") Boolean allUploads) {
		// CREATE QUERY
		StringBuilder query = new StringBuilder("select * from PmsiUpload ");

		// DETECTS IF WE HAVE TO SEE ONLY PENDING UPLOADS
		if (allUploads != null && allUploads)
			query.append("where processed=false ");
		
		// ORDER RESULTS
		List<String> orderquery = new LinkedList<>();
		// IF SOME ORDER IS DEFINED
		if (order != null && order.size() != 0) {
			for (int i = 0 ; i < orderelts.size() ; i++) {
				// SEARCHES IF THIS INDEX IS DEFINED IN orderindex
				String orderfield = orderindex.get(orderelts.get(i));
				if (orderfield != null) {
					// THIS FIELD IS KNOWN, WE HAVE TO KNOW IF THE ORDER IS ASCENDING OR DESCENDING
					if (order != null && order.size() >= i && order.get(i)) {
						orderquery.add(orderfield + " DESC ");
					} else {
						orderquery.add(orderfield + " ASC ");
					}

				}
			}
		}
		// IF THE DEFINED ORDERS HAVE TO BE TRANSCRIPTED IN QUERY
		if (orderquery.size() != 0) {
			query.append("order by ");
			for (String element : orderquery) {
				query.append(element);
			}
		}

		// DEFINES THE WINDOW
		query.append("offset ").append(first).append(" limit ").append(rows + 1);

		// EXECUTES THE QUERY
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(query.toString());
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getConnection();
			tx.begin();
			results = tx.command(oquery).execute();
			tx.commit();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		} finally {
			if (tx != null)
				tx.close();
		}

		// ENTERS THE ELEMENTS IN THE RESULTS LIST
		UploadedElements upelts = new UploadedElements();
		upelts.setLastChunk(true);
		upelts.setOnlyPending(allUploads);
		upelts.setOrder(orderelts);
		upelts.setOrderdir(order);
		upelts.setAskedFirst(first);
		upelts.setAskedRows(rows);
		// LIST OF THE ELEMENTS
		List<UploadedElement> upeltslist = new LinkedList<>();
		upelts.setElement(upeltslist);
		for (int i = 0 ; i < results.size() ; i++) {
			if (i > rows) {
				upelts.setLastChunk(false);
				break;
			}
			ODocument result = results.get(i);
			UploadedElement element = new UploadedElement();
			element.setRecordId(result.getIdentity());
			element.setDateEnvoi((Date) result.field("dateenvoi"));
			element.setFiness((String) result.field("finess"));
			element.setMonth((Integer) result.field("month"));
			element.setProcessed((Boolean) result.field("processed"));
			element.setYear((Integer) result.field("year"));
			element.setSuccess((Boolean) result.field("success"));
			element.setRowNumber(new Long(first + i));
			if (result.field("rss") == null)
				element.setComment("RSF");
			else
				element.setComment("RSF et RSS");
			upeltslist.add(element);
		}

		return upelts;
	}
}
