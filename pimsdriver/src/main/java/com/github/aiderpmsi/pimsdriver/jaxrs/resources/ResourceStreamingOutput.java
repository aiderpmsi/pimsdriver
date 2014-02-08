package com.github.aiderpmsi.pimsdriver.jaxrs.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class ResourceStreamingOutput implements StreamingOutput {

	private static final int BUF_SIZE = 1024;

	private String resource;
	
	private String extension;
	
	private ServletContext context;
	
	public ResourceStreamingOutput(String resource, String extension, ServletContext context) {
		this.resource = resource;
		this.extension = extension;
		this.context = context;
	}
	
	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		// GET THE RESOURCE FROM META-INF CLASSPATH
		String resourceloc = "/META-INF/" + extension + "/" + resource + "." + extension;
		final InputStream is = context.getResourceAsStream(resourceloc);
		// DEFINE THIS INPUTSTREAM AS RESULT
		byte[] buf = new byte[BUF_SIZE];
		int size;
		while ((size = is.read(buf)) != -1) {
			output.write(buf, 0, size);
		}
	}

}
