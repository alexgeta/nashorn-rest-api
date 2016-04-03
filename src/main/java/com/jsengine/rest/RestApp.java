package com.jsengine.rest;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class RestApp extends ResourceConfig {
    public RestApp() {
        packages("com.jsengine");
    }
}
