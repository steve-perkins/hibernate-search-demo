package net.steveperkins.hibernatesearchdemo.util;

import java.util.StringTokenizer;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.QueryWrapperFilter;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.impl.CachingWrapperFilter;

/**
 * This class provides logic for filtering "App" entities on the basis of whether or not their "supportedDevices"
 * field includes a "Device" with a given name.  This factory class is associated with the "App" entity class 
 * through a @FullTextFilterDef annotation.
 */
public class DeviceFilterFactory {

	private String deviceName;
	
	/**
	 * When a filter factory takes parameters (e.g. "deviceName"), or does not go out of its way to disable the 
	 * filter caching that is enabled by default... then it must have a method, annotated with @Key, that produces 
	 * a FilterKey object.  This method here uses a custom class based on the "deviceName" parameter (see DeviceFilterKey).    
	 */
	@Key
	public FilterKey getKey() {
		DeviceFilterKey key = new DeviceFilterKey();
		key.setDeviceName(this.deviceName);
		return key;
	}
	
	/**
	 * When a @FullTextFilterDef annotation associates this factory class with a given name, and a "FullTextQuery.enableFullTextFilter()" is 
	 * called with that name as its input parameter, then this method is used to return a Filter with the actual filtering logic.  It is 
	 * the @Factory annotation that designates this method as having that responsibility for this factory class. 
	 */
	@Factory
	public Filter getFilter() {
		StringTokenizer tokenzier = new StringTokenizer(deviceName.toLowerCase());
		PhraseQuery query = new PhraseQuery();
		while(tokenzier.hasMoreTokens()) {
			// By default, field values were converted to lower-case when indexed by Lucene.  So be sure to 
			// convert search terms to lower-case in order to make them match.
			Term term = new Term("supportedDevices.name", tokenzier.nextToken().toLowerCase());
			query.add(term);
		}
		Filter filter = new QueryWrapperFilter(query);
		return new CachingWrapperFilter(filter);
	}
	
	public String getDeviceName() {
		return this.deviceName;
	}
	
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName.toLowerCase();
	}
	
}
