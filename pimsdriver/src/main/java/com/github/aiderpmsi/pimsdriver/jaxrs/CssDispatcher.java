package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Path("/css") 
public class CssDispatcher {
	
	@GET
    @Path("/{resource}")
    @Produces({MediaType.TEXT_PLAIN})
    public StreamingOutput cssResource(@PathParam("resource") String resource) {
				
		return new StreamingOutput() {
			
			private static final int BUF_SIZE = 1024;
			
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				final InputStream is = new ByteArrayInputStream("Hello Guys!".getBytes());
				byte[] buf = new byte[BUF_SIZE];
				int size;
				while ((size = is.read(buf)) != 0) {
					output.write(buf, 0, size);
				}
			}
		};
    }
	
}
