package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.github.aiderpmsi.pimsdriver.processor.ProcessListener;
import com.github.aiderpmsi.pimsdriver.security.SecurityFilter;

public class PimsDriverApplication extends Application {

	@Override
    public Set<Class<?>> getClasses() {
		// SET JAXRS CLASSES
        final Set<Class<?>> classes = new HashSet<>();
        // register jax-rs resources
        classes.add(ImportPmsi.class);
        classes.add(UploadedPmsi.class);
        classes.add(Report.class);
        // register filters
        classes.add(RolesAllowedDynamicFeature.class);
        classes.add(SecurityFilter.class);
        // register multipart
        classes.add(MultiPartFeature.class);
        // register listeners
        classes.add(ProcessListener.class);
        return classes;
    }

}
