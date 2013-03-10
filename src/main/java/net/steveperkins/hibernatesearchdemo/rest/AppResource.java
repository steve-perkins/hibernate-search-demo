package net.steveperkins.hibernatesearchdemo.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.steveperkins.hibernatesearchdemo.domain.App;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAX-RS RESTful service for fetching an App entity by its ID.  Thanks to this service, the search 
 * servlet can use projection-based search queries against the Lucene index alone... only hitting the 
 * database when the "Full Details" button is clicked for a particular app.  That button triggers an 
 * AJAX request for this service, and this service returns an App entity formatted as a JSON object.
 * 
 * The "RestfulApplication" class in this package registers this service and makes it available for use.
 * 
 * The @Path annotation defines the URL for which this service handles requests.  In conjunction with 
 * base path defined in "RestfulApplication", the full path would look like:  http://localhost:8080/rest/appById/1.
 * 
 * The @Produces annotation tells JAX-RS to convert the return object into JSON format.
 */
@Path("/appById/{appId}")
@Produces(MediaType.APPLICATION_JSON)
public class AppResource {
	
	Logger logger = LoggerFactory.getLogger(AppResource.class);

	/**
	 * The @GET annotation causes this method to be invoked whenever an HTTP GET request is received for 
	 * the registered URL. 
	 */
	@GET
	public App getAppData( @PathParam("appId") Long appId ) {
		// Initialize Hibernate
		Configuration configuration = new Configuration();
		configuration.configure();
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		
		// Fetch an App for the given ID, using eager fetching.  The conversion to JSON happens after the 
		// Hibernate Session is closed... so if lazy fetching were used, then the JSON converter would fail 
		// when trying to access associated objects.
		Criteria criteria = session.createCriteria(App.class);
		criteria.add( Restrictions.eq("id", appId) );
		criteria.setFetchMode("supportedDevices", FetchMode.SELECT);
		criteria.setFetchMode("customerReviews", FetchMode.SELECT);
		App app = (App) criteria.uniqueResult();
		
		// Cleanup Hibernate
		session.getTransaction().commit();
		session.clear();
		session.close();
		sessionFactory.close();
		
		return app;
	}
	
}
