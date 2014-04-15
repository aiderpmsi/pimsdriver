package com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;

@Path("/process") 
@PermitAll
public class ProcessPmsi extends ProcessPmsiBase {

	@SuppressWarnings("serial")
	public static final Set<String> orderindex = new HashSet<String>(5){{
		add("dateenvoi");
		add("month");
		add("year");
		add("finess");
		add("processed");
	}};

	@GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML})
	public List<UploadedElement> getPendingUploadedElements(
			@DefaultValue("0") @QueryParam("first") Integer first,
			@DefaultValue("20") @QueryParam("rows") Integer rows,
			@QueryParam("orderelts") List<String> orderelts,
			@QueryParam("order") List<Boolean> order) {

		// CREATE QUERY
		StringBuilder query = new StringBuilder("SELECT * FROM PmsiUpload WHERE processed='waiting' ");

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
		return getPendingUploadedElements(query.toString());
	}
	
}