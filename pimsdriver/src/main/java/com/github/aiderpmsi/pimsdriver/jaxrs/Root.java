package com.github.aiderpmsi.pimsdriver.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.views.RootElement;

@Path("/question") 
public class Root {
	
	@GET
    @Path("/root")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"\"?>")
    public RootElement ping() {
		
		RootElement element = new RootElement();
		element.setElement("coucou");
		
        return element;
    }

}
