package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.github.aiderpmsi.pimsdriver.jaxrs.importpmsi.ImportRsf;
import com.github.aiderpmsi.pimsdriver.jaxrs.processpmsi.ProcessPmsi;
import com.github.aiderpmsi.pimsdriver.jaxrs.resources.ResourceDispatcher;
import com.github.aiderpmsi.pimsdriver.processor.ProcessListener;
import com.github.aiderpmsi.pimsdriver.security.SecurityFilter;
import com.github.aiderpmsi.pismdriver.xsltfilter.XsltFilter;

@ApplicationPath("/")
public class PimsDriverApplication extends Application {

	@Override
    public Set<Class<?>> getClasses() {
		// SET JAXRS CLASSES
        final Set<Class<?>> classes = new HashSet<>();
        // register jax-rs resources
        classes.add(Welcome.class);
        classes.add(ResourceDispatcher.class);
        classes.add(ImportRsf.class);
        classes.add(ProcessPmsi.class);
        // register filters
        classes.add(RolesAllowedDynamicFeature.class);
        classes.add(SecurityFilter.class);
        classes.add(XsltFilter.class);
        // register multipart
        classes.add(MultiPartFeature.class);
        // register listeners
        classes.add(ProcessListener.class);
        return classes;
    }

}
