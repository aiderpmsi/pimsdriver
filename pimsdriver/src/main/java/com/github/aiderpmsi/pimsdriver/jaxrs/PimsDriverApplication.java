package com.github.aiderpmsi.pimsdriver.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class PimsDriverApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>(1);
        // register jax-rs resources
        classes.add(Root.class);
        classes.add(CssDispatcher.class);
        // register filters
        classes.add(SecurityFilter.class);
        return classes;
    }
}
