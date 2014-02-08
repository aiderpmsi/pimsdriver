package com.github.aiderpmsi.pimsdriver.jaxrs;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.message.XmlHeader;

import com.github.aiderpmsi.pimsdriver.views.WelcomeElement;

@Path("/welcome") 
@PermitAll
public class Welcome {
	
	@GET
    @Path("/main")
    @Produces({MediaType.APPLICATION_XML})
	@XmlHeader("<?xml-stylesheet type=\"text/xsl\" href=\"../resources/xslt/welcome.xslt\"?>")
    public WelcomeElement ping() {
		
		WelcomeElement element = new WelcomeElement();
		element.setElement("coucou");
		
        return element;
    }

}
