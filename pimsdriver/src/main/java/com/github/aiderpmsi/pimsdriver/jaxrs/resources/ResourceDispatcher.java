package com.github.aiderpmsi.pimsdriver.jaxrs.resources;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Path("/resources") 
@PermitAll
public class ResourceDispatcher {
	
	@GET
    @Path("/{resource : [a-zA-Z]{1,15}}.{extension : (css)|(xslt)}")
    @Produces({MediaType.TEXT_PLAIN})
    public StreamingOutput resource(
    		@PathParam("resource") final String resource,
    		@PathParam("extension") final String extension,
    		@Context ServletContext context) {
				
		return new ResourceStreamingOutput(resource, extension, context);
    }
	
}
