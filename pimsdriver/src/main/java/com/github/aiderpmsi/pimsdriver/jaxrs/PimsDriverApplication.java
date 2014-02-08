package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.github.aiderpmsi.pimsdriver.jaxrs.resources.ResourceDispatcher;
import com.github.aiderpmsi.pimsdriver.security.SecurityFilter;

@ApplicationPath("/")
public class PimsDriverApplication extends Application {

	@Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        // register jax-rs resources
        classes.add(Welcome.class);
        classes.add(ResourceDispatcher.class);
        // register filters
        classes.add(RolesAllowedDynamicFeature.class);
        classes.add(SecurityFilter.class);
        return classes;
    }

}
