package com.github.aiderpmsi.pimsdriver.jaxrs;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.message.XmlHeader;

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
    		@Context ServletContext context) {
		
		HtmlHelper help = new HtmlHelper()
			.setContext(context)
			.setModel(mainXml())
			.setXslResource("welcome");

		return help;
    }
}
