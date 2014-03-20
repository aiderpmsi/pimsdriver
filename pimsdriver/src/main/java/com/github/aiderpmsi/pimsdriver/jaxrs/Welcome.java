package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.jaxrs.resources.ResourceStreamingOutput;

@Path("/welcome") 
@PermitAll
public class Welcome {
	
	@GET
    @Path("/main")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/welcome.xslt\"?>")
    public VoidElement mainXml() {
		
		VoidElement element = new VoidElement();
		
        return element;
    }

	@GET
    @Path("/main")
    @Produces({MediaType.TEXT_HTML})
    public StreamingOutput mainHtml(
    		@Context ServletContext context) throws JAXBException, TransformerConfigurationException, IOException {
		
		// GETS THE JAXBCONTEXT

		JAXBContext jaxbContext = JAXBContext.newInstance(VoidElement.class);
		
		// CREATE THE SAXON TRANSFORMATION
		TransformerFactory tfactory = TransformerFactory.newInstance();
		final Transformer transformer = tfactory.newTransformer(
				new StreamSource((new ResourceStreamingOutput("welcome", "xslt", context)).getAsInputStream()));
		
		// CREATES THE STREAMING OUTPUT
		final JAXBSource source = new JAXBSource(jaxbContext, mainXml());
	    
		StreamingOutput ret = new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				StreamResult str = new StreamResult(output);
				try {
					transformer.transform(source, str);
				} catch (TransformerException e) {
					throw new IOException(e);
				}
			}
		};

		return ret;
    }
}
