package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.github.aiderpmsi.pimsdriver.security.ExternalUser;
import com.github.aiderpmsi.pimsdriver.security.SecurityContextImpl;

@Provider
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {
	 
    @Override
    public void filter(ContainerRequestContext requestContext)
                    throws IOException {

    	// OVERRIDES THE CONTAINERS SECURITY CONTEXT
        final SecurityContext securityContext =
                    new SecurityContextImpl(new ExternalUser());

        requestContext.setSecurityContext(securityContext);
    }
}
