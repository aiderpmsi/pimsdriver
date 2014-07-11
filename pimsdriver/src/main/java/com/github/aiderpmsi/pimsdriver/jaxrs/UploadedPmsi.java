package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.github.aiderpmsi.pimsdriver.db.actions.ActionException;
import com.github.aiderpmsi.pimsdriver.db.actions.NavigationActions;
import com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsis;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;

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
	public Response getPendingUploadedElements(
			@DefaultValue("0") @QueryParam("first") Integer first,
			@DefaultValue("20") @QueryParam("rows") Integer rows,
			@QueryParam("orderelts") List<String> orderelts,
			@QueryParam("order") List<Boolean> order,
			@Context ServletContext context) {

		// CREATE FILTERS AND ORDERS
		List<Filter> filters = new ArrayList<>(1);
		filters.add(new Compare.Equal("plud_processed", "pending"));
		List<OrderBy> orders = new ArrayList<>(orderelts.size());
		for (int i = 0 ; i < orderelts.size() ; i++) {
			if (orderindex.containsKey(orderelts.get(i))) {
				OrderBy orderelt = new OrderBy(orderindex.get(orderelts.get(i)), 
						order.size() < i ? true : order.get(i));
				orders.add(orderelt);
			}
		}

		NavigationActions na = new NavigationActions(context);
		try {
			List<com.github.aiderpmsi.pimsdriver.dto.model.UploadedPmsi> pued =
					na.getUploadedPmsi(filters, orders, first, rows);
			UploadedPmsis pueds = new UploadedPmsis();
			pueds.setElements(pued);
			return Response.ok(pueds).build();
		} catch (ActionException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
}