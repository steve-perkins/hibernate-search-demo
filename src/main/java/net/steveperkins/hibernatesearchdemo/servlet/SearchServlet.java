package net.steveperkins.hibernatesearchdemo.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.steveperkins.hibernatesearchdemo.domain.App;
import net.steveperkins.hibernatesearchdemo.util.StartupDataLoader;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.facet.Facet;
import org.hibernate.search.query.facet.FacetSortOrder;
import org.hibernate.search.query.facet.FacetingRequest;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A controller/model servlet that processes a search, and renders the search result JSP/JSTL view.
 * 
 * The Servlet 3.0 annotation @WebServlet maps this servlet to the URL "search" (e.g. "http://localhost:8080/search").  With 
 * earlier versions of the Servlet spec, this configuration would belong in the "web.xml" file.  The basic logic of this 
 * search operation could be refactored for an application using Spring, JSF, or any other Java-based application framework. 
 */
@SuppressWarnings("serial")
@WebServlet("search")
public class SearchServlet extends HttpServlet {

	/**
	 * This method contains the primary search functionality for this servlet, and is automatically invoked once for every HTTP
	 * POST to the mapped URL. 
	 */
	@SuppressWarnings("unchecked")
	@Override	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Logger logger = LoggerFactory.getLogger(SearchServlet.class);
		
		// Get the user's search keyword(s).  Get optional parameters, or apply default values if those parameters weren't passed.
		String searchString = request.getParameter("searchString") != null ? request.getParameter("searchString").trim() : "";
		String selectedDevice = request.getParameter("selectedDevice") != null ? request.getParameter("selectedDevice").trim() : "all";
		String selectedCategory = request.getParameter("selectedCategory") != null ? request.getParameter("selectedCategory") : "all";
		String selectedPriceRange = request.getParameter("selectedPriceRange") != null ? request.getParameter("selectedPriceRange") : "all";
		String sortField = request.getParameter("sortField") != null ? request.getParameter("sortField").trim() : "relevance";
		int firstResult = request.getParameter("firstResult") != null ? Integer.parseInt(request.getParameter("firstResult")) : 0;
		logger.info("Received searchString [" + searchString 
				+ "], selectedDevice [" + selectedDevice
				+ "], selectedCategory [" + selectedCategory
				+ "], selectedPriceRange [" + selectedPriceRange
				+ "], sortField [" + sortField 
				+ "], and firstResult [" +  firstResult + "]");

		// Start a Hibernate session.
		Session session = StartupDataLoader.openSession();
		
		// Create a Hibernate Search wrapper around the vanilla Hibernate session
		FullTextSession fullTextSession = Search.getFullTextSession(session);

		// Begin a transaction.  This may not be strictly necessary, but is a good practice in general.
		fullTextSession.beginTransaction();

		// Create a Hibernate Search QueryBuilder for the appropriate Lucene index (i.e. the index for "App" in this case)
		QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity( App.class ).get();
		
		// Use the QueryBuilder to construct a Lucene query... matching the user's search keywords against the "name" 
		// and "description" fields of App, as well as "name" field of associated Device entities, and the "comments" field of
		// embedded CustomerReview objects.
		org.apache.lucene.search.Query luceneQuery = null;
		if(searchString.length() > 2 && searchString.startsWith("\"") && searchString.endsWith("\"")) {
			
			// If the user's search string is surrounded by double-quotes, then use a phrase search.
			// Boost the "name" and "description" fields even more so than normal (to showcase query-time boosting
			// as well as the index-time boosting already seen).
			String unquotedSearchString = searchString.substring(1, searchString.length() - 1);
			luceneQuery = queryBuilder
					.phrase()
					.onField("name").boostedTo(2)
					.andField("description").boostedTo(2)
					.andField("supportedDevices.name").andField("customerReviews.comments")
					.sentence(unquotedSearchString)
					.createQuery();
		} else {
			
			// If the user's search string is not quoted, then use a fuzzy keyword search
			luceneQuery = queryBuilder
					.keyword()
					.fuzzy()
					.withThreshold(0.7f)
					.onFields("name", "description", "supportedDevices.name", "customerReviews.comments")
					.matching(searchString)
					.createQuery();
		}
		FullTextQuery hibernateQuery = fullTextSession.createFullTextQuery(luceneQuery, App.class);  // could be cast to "org.hibernate.Query"

		// Use a FullTextFilter to include only those apps supported on a particular device 
		if(selectedDevice != null && !selectedDevice.equals("all")) {
			hibernateQuery.enableFullTextFilter("deviceName").setParameter("deviceName", selectedDevice);
		}
		
		// Apply optional sort criteria, if not the default sort-by-relevance
		if(sortField.equals("name")) {
			Sort sort = new Sort(new SortField("sorting_name", SortField.STRING));
			hibernateQuery.setSort(sort);
		} else if(sortField.equals("name-reverse")) {
			Sort sort = new Sort(new SortField("sorting_name", SortField.STRING, true));
			hibernateQuery.setSort(sort);
		}

		// Get the estimated number of results (NOTE: not 100% accurate, but doesn't require a database hit that 
		// might be resource-intensive for large data sets).
		int resultSize = hibernateQuery.getResultSize();

		// Limit search to a batch of 5 results, and starting where the last page left off (default to beginning)
		logger.info("Query string == " + hibernateQuery.getQueryString());
		hibernateQuery.setFirstResult(firstResult);
		hibernateQuery.setMaxResults(5);
		
		// Use a projection to get field data from the Lucene index, without having to hit the database.  We don't need all 
		// of each App object's properties... just the ones we want to display right now, and the "id" so we might fetch the 
		// full object later.
		hibernateQuery.setProjection("id", "name", "description", "image");
		// Since we are using projection, "hibernateQuery.list()" below will normally return "Object[]" (i.e. one array element 
		// per projected field).  This ResultTransformer uses those values to build detached App objects, and makes "hibernateQuery.list()"
		// return the usual "List<App>".
		hibernateQuery.setResultTransformer( new AliasToBeanResultTransformer(App.class) );		
		
		// Create a discrete faceting request... to determine which categories are represented in the search results, and 
		// how many results are in each category.  This information won't be accessible until after the query is actually 
		// performed (i.e. by calling "hibernateQuery.list()").
		FacetingRequest categoryFacetingRequest = queryBuilder
				.facet()
				.name("categoryFacet")
				.onField("category")
				.discrete()
				.orderedBy(FacetSortOrder.FIELD_VALUE)
				.includeZeroCounts(false)
				.createFacetingRequest();
		hibernateQuery.getFacetManager().enableFaceting(categoryFacetingRequest);
		
		// Create a ranged faceting request... to determine which search results fit into price bands of "under $1", "$1-$5", 
		// or "above $5".  This information won't be accessible until after the query is actually performed (i.e. by calling 
		// "hibernateQuery.list()").
		FacetingRequest priceRangeFacetingRequest = queryBuilder
				.facet()
				.name("priceRangeFacet")
				.onField("price")
				.range()
				.below(1f).excludeLimit()
				.from(1f).to(5f)
				.above(5f).excludeLimit()
				.createFacetingRequest();
		hibernateQuery.getFacetManager().enableFaceting(priceRangeFacetingRequest);
		
		// Apply a limit of two seconds on the amount of time the search query may run.  If the query has not completed after 
		// two seconds, then it should stop gracefully and return the list of results found so far.
		hibernateQuery.limitExecutionTimeTo(2, TimeUnit.SECONDS);
		
		// Actually perform the search
		List<App> apps = hibernateQuery.list();
		
		// Now that the search has been performed, information can be obtained about about the facets enabled a couple of steps back. 
		List<Facet> categoryFacets = hibernateQuery.getFacetManager().getFacets("categoryFacet");
		List<Facet> priceRangeFacets = hibernateQuery.getFacetManager().getFacets("priceRangeFacet");
		
		// Use the category facet information to build a list of categories found in this data set.
		Map<String, Integer> categories = new TreeMap<String, Integer>();
		for(Facet categoryFacet : categoryFacets) {
			categories.put(categoryFacet.getValue(), categoryFacet.getCount());
			// If the "selectedCategory" CGI parameter was passed to this servlet, then it means that a particular category has been 
			// selected on the search results JSP.  Apply the matching facet to the query so it can be re-run, limiting results to
			// that category.
			if(categoryFacet.getValue().equalsIgnoreCase(selectedCategory)) {
				hibernateQuery.getFacetManager().getFacetGroup("categoryFacet").selectFacets(categoryFacet);
			}
		}
		// Use the price range facet information to build a list of price bands found in this data set.
		Map<String, Integer> priceRanges = new TreeMap<String, Integer>();
		for(Facet priceRangeFacet : priceRangeFacets) {
			priceRanges.put(priceRangeFacet.getValue(), priceRangeFacet.getCount());
			// If the "selectedPriceRange" CGI parameter was passed to this servlet, then it means that a particular price range 
			// has been selected on the search results JSP.  Apply the matching facet to the query so it can be re-run, limiting 
			// results to that category.
			if(priceRangeFacet.getValue().equalsIgnoreCase(selectedPriceRange)) {
				hibernateQuery.getFacetManager().getFacetGroup("priceRangeFacet").selectFacets(priceRangeFacet);
			}
		}
		// Re-run the search query.  If one or more facets have been selected, then they will be applied... otherwise, the 
		// results will not change.
		apps = hibernateQuery.list();
		
		// Estimate the number of search results found, to help with pagination.
		resultSize = hibernateQuery.getResultSize();
		
		// Put the search results on the HTTP request object, along with sorting and pagination related parameters
		request.setAttribute("searchString", searchString);
		request.setAttribute("selectedDevice", selectedDevice);
		request.setAttribute("selectedCategory", selectedCategory);
		request.setAttribute("selectedPriceRange", selectedPriceRange);
		request.setAttribute("sortField", sortField);
		request.setAttribute("apps", apps);
		request.setAttribute("resultSize", resultSize);
		request.setAttribute("firstResult", firstResult);
		request.setAttribute("categories", categories);
		request.setAttribute("priceRanges", priceRanges);

		// Close and clean up the Hibernate session
		fullTextSession.getTransaction().commit();
		session.close();
		
		// Forward the request object (including the search results) to the JSP/JSTL view for rendering
		getServletContext().getRequestDispatcher("/WEB-INF/pages/search.jsp").forward(request, response);
	}

	/**
	 * This method is automatically invoked once for every HTTP GET to the mapped URL.  For this servlet, the same code should 
	 * be executed regardless of whether the request is a POST or GET... so this method simply forwards the request to "doPost()".
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		this.doPost(request, response);
	}
	
}
