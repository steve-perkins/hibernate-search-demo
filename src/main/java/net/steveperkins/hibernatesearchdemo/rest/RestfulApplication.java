package net.steveperkins.hibernatesearchdemo.rest;

import javax.ws.rs.ApplicationPath;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

/**
 * This class ultimately inherits from "javax.ws.rs.core.Application", and registers JAX-RS RESTful services for use.  There 
 * is only one service in this application, see the "AppResource" class.
 * 
 * The @ApplicationPath annotation declares the base path for service URL's.
 */
@ApplicationPath("/rest")
public class RestfulApplication extends PackagesResourceConfig {

	public RestfulApplication() {
		// This Java package will be automatically scanned for JAX-RS services to register.
		super("net.steveperkins.hibernatesearchdemo.rest");
		
		// Jersey is the JAX-RS implementation being used in this application.  The Jersey feature 
		// below makes the Jackson parser available for converting objects to JSON format.
		getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
	}
	
}
