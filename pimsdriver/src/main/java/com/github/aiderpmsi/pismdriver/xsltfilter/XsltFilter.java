package com.github.aiderpmsi.pismdriver.xsltfilter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class XsltFilter implements WriterInterceptor {

	@Override
	public void aroundWriteTo(WriterInterceptorContext context)
			throws IOException, WebApplicationException {
		// LOOK IF THIS IS XML
		context.getMediaType();
		
		// LOOK IF HTML IS ACCEPTED
		MultivaluedMap<String, Object> headers = context.getHeaders();
		headers.toString();
		
		context.setOutputStream(context.getOutputStream());
		}

}
