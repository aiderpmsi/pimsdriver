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
    @Path("/xslt/{resource : [a-zA-Z]{1,15}}.xslt")
    @Produces({MediaType.APPLICATION_XML})
    public StreamingOutput resourceXslt(
    		@PathParam("resource") final String resource,
    		@Context ServletContext context) {
				
		return new ResourceStreamingOutput(resource, "xslt", context);
    }
	
	@GET
    @Path("/css/{resource : [a-zA-Z]{1,15}}.css")
    @Produces({"text/css"})
    public StreamingOutput resourceCss(
    		@PathParam("resource") final String resource,
    		@Context ServletContext context) {
				
		return new ResourceStreamingOutput(resource, "css", context);
    }

	@GET
    @Path("/images/{folder : [a-zA-Z]{1,15}}.{resource : [a-zA-Z]{1,15}}.png")
    @Produces({"image/png"})
    public StreamingOutput resourceImage(
    		@PathParam("folder") final String folder,
    		@PathParam("resource") final String resource,
    		@Context ServletContext context) {
				
		return new ResourceStreamingOutput(folder + "/" + resource, "png", context);
    }

}
