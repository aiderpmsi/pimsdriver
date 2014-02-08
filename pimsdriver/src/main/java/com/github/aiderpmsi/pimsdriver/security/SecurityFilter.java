package com.github.aiderpmsi.pimsdriver.security;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
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
