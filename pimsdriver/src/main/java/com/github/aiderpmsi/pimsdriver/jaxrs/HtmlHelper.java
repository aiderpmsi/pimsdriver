package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;

import com.github.aiderpmsi.pimsdriver.jaxrs.resources.ResourceStreamingOutput;

public class HtmlHelper implements StreamingOutput {

	private ServletContext context = null;
	
	private Object model = null;
	
	private String xslResource = null;

	@Override
	public void write(OutputStream output) throws IOException,
			WebApplicationException {
		try {
			// JAXB CONTEXT
			JAXBContext jaxbContext = JAXBContext.newInstance(model.getClass());
		
			// SAXON TRANSFORMER FACTORY AND TRANSFORMATION
			TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl(new Configuration());
			final Transformer transformer = tfactory.newTransformer(
					new StreamSource((new ResourceStreamingOutput(xslResource, "xslt", context)).getAsInputStream()));
			
			// CREATES THE STREAMING INPUT
			final JAXBSource source = new JAXBSource(jaxbContext, model);

			// FINAL DESTINATION
			StreamResult str = new StreamResult(output);
			
			// TRANSFORMATION
			transformer.transform(source, str);
		} catch (TransformerException | JAXBException e) {
			throw new IOException(e);
		}
	}
	
	public ServletContext getContext() {
		return context;
	}

	public HtmlHelper setContext(ServletContext context) {
		this.context = context;
		return this;
	}

	public Object getModel() {
		return model;
	}

	public HtmlHelper setModel(Object model) {
		this.model = model;
		return this;
	}

	public String getXslResource() {
		return xslResource;
	}

	public HtmlHelper setXslResource(String xslResource) {
		this.xslResource = xslResource;
		return this;
	}

}
