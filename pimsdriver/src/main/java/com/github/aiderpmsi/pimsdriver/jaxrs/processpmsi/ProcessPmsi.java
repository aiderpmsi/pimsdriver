package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.odb.DocDbConnectionFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

@Path("/process") 
@PermitAll
public class ProcessPmsi {
	
	@SuppressWarnings("serial")
	public static final Set<String> orderindex = new HashSet<String>(5){{
		add("dateenvoi");
		add("month");
		add("year");
		add("finess");
		add("processed");
	}};

	@GET
    @Path("/process/{recordId : [+-]?[0-9]+:[+-]?[0-9]+}")
	public Response setProcessable(
			@PathParam("recordId") String recordId,
			@DefaultValue("0") @QueryParam("first") Integer first,
			@DefaultValue("20") @QueryParam("rows") Integer rows,
			@QueryParam("orderelts") List<String> orderelts,
			@QueryParam("order") List<Boolean> order,
			@DefaultValue("true") @QueryParam("onlyPending") Boolean onlyPending,
			@Context UriInfo uriInfo) throws IOException {

		// SETS THE RECORD AS PROCESSABLE
		ODatabaseDocumentTx tx = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			OCommandSQL command =
					new OCommandSQL("update PmsiUpload set processed = 'pending' WHERE @RID=? AND processed='waiting'");
			tx.command(command).execute(new ORecordId("#" + recordId));
			tx.commit();
		} finally {
			if (tx != null) {
				tx.close();
				tx = null;
			}
		}

		UriBuilder redirectionBuilder = uriInfo.getBaseUriBuilder().
				path(ProcessPmsi.class).
				path(ProcessPmsi.class, "getElements");
		if (first != null) redirectionBuilder.queryParam("first", first);
		if (rows != null) redirectionBuilder.queryParam("rows", rows);
		if (orderelts != null) {
			for (String orderelt : orderelts) {
				redirectionBuilder.queryParam("orderelts", orderelt);
			}
		}
		if (order != null) {
			for (Boolean orde : order) {
				redirectionBuilder.queryParam("order", orde);
			}
		}
		if (onlyPending != null) redirectionBuilder.queryParam("onlyPending", onlyPending);

		ResponseBuilder resp = Response.seeOther(redirectionBuilder.build());

		return resp.build();
	}

	@GET
    @Path("/list")
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/processlist.xslt\"?>")
    @Produces({MediaType.APPLICATION_XML})
	public UploadedElements getElements(
			@DefaultValue("0") @QueryParam("first") Integer first,
			@DefaultValue("20") @QueryParam("rows") Integer rows,
			@QueryParam("orderelts") List<String> orderelts,
			@QueryParam("order") List<Boolean> order,
			@DefaultValue("true") @QueryParam("onlyPending") Boolean onlyPending) {
		// CREATE QUERY
		StringBuilder query = new StringBuilder("select * from PmsiUpload ");

		// DETECTS IF WE HAVE TO SEE ONLY PENDING UPLOADS
		if (onlyPending != null && onlyPending)
			query.append("where processed='waiting' ");
		
		// ORDER RESULTS
		List<String> orderquery = new LinkedList<>();
		// IF SOME ORDER IS DEFINED
		if (order != null && order.size() != 0) {
			for (int i = 0 ; i < orderelts.size() ; i++) {
				// SEARCHES IF THIS INDEX IS DEFINED IN orderindex
				String orderfield = orderelts.get(i);
				if (orderindex.contains(orderfield)) {
					// THIS FIELD IS KNOWN, WE HAVE TO KNOW IF THE ORDER IS ASCENDING OR DESCENDING
					if (order != null && order.size() >= i && order.get(i)) {
						orderquery.add(orderfield + " DESC");
					} else {
						orderquery.add(orderfield + " ASC");
					}

				}
			}
		}
		// IF THE DEFINED ORDERS HAVE TO BE TRANSCRIPTED IN QUERY
		if (orderquery.size() != 0) {
			query.append("order by ");
			query.append(StringUtils.join(orderquery, ", "));
			query.append(" ");
		}

		// DEFINES THE WINDOW
		query.append("offset ").append(first).append(" limit ").append(rows + 1);

		// EXECUTES THE QUERY
		OSQLSynchQuery<ODocument> oquery = new OSQLSynchQuery<ODocument>(query.toString());
		ODatabaseDocumentTx tx = null;
		List<ODocument> results = null;
		try {
			tx = DocDbConnectionFactory.getInstance().getConnection();
			tx.begin();
			results = tx.command(oquery).execute();
			tx.commit();
		} finally {
			if (tx != null)
				tx.close();
		}

		// ENTERS THE ELEMENTS IN THE RESULTS LIST
		UploadedElements upelts = new UploadedElements();
		upelts.setLastChunk(!(results.size() > rows));
		upelts.setOnlyPending(onlyPending);
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
			element.setProcessed((String) result.field("processed"));
			element.setYear((Integer) result.field("year"));
			element.setSuccess((Boolean) result.field("success"));
			element.setRowNumber(new Long(first + i));
			if (result.field("rss") == null)
				element.setComment("RSF seul");
			else
				element.setComment("RSF et RSS");
			upeltslist.add(element);
		}

		return upelts;
	}
}
