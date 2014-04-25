package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.github.aiderpmsi.pimsdriver.dao.UploadedElementsDTO;
import com.github.aiderpmsi.pimsdriver.model.PmsiUploadedElementModel;

@Path("/uploaded") 
@PermitAll
public class UploadedPmsi {

	@SuppressWarnings("serial")
	public static final HashMap<String, String> orderindex = new HashMap<String, String>(5){{
		put("dateenvoi", "plud_dateenvoi");
		put("month", "plud_month");
		put("year", "plud_year");
		put("finess", "plud_year");
		put("processed", "plud_processed");
	}};

	@GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML})
	public List<PmsiUploadedElementModel> getPendingUploadedElements(
			@DefaultValue("0") @QueryParam("first") Integer first,
			@DefaultValue("20") @QueryParam("rows") Integer rows,
			@QueryParam("orderelts") List<String> orderelts,
			@QueryParam("order") List<Boolean> order) {

		// CREATE QUERY
		StringBuilder query = new StringBuilder(
				"SELECT plud_id, plud_processed, plud_finess, "
				+ "plud_year, plud_month, plud_dateenvoi, plud_rsf_oid oid, "
				+ "plud_rss_oid, plud_arguments FROM PmsiUpload WHERE processed='pending' ");

		// ORDER RESULTS
		List<String> orderquery = new LinkedList<>();
		// IF SOME ORDER IS DEFINED
		if (order != null && order.size() != 0) {
			for (int i = 0 ; i < orderelts.size() ; i++) {
				// SEARCHES IF THIS INDEX IS DEFINED IN orderindex
				String orderfield = orderelts.get(i);
				if (orderindex.containsKey(orderfield)) {
					// THIS FIELD IS KNOWN, WE HAVE TO KNOW IF THE ORDER IS ASCENDING OR DESCENDING
					if (order != null && order.size() >= i && order.get(i)) {
						orderquery.add(orderindex.get(orderfield) + " DESC");
					} else {
						orderquery.add(orderindex.get(orderfield) + " ASC");
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
		UploadedElementsDTO ued = new UploadedElementsDTO();
		return ued.getUploadedElements(query.toString(), new Object[]{});
	}
	
}