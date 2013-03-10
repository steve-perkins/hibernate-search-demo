package net.steveperkins.hibernatesearchdemo.domain;

import javax.persistence.Embeddable;

import net.steveperkins.hibernatesearchdemo.util.FiveStarBoostStrategy;

import org.apache.solr.analysis.HTMLStripCharFilterFactory;
import org.apache.solr.analysis.StandardTokenizerFactory;
import org.apache.solr.analysis.StandardFilterFactory;
import org.apache.solr.analysis.StopFilterFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.CharFilterDef;
import org.hibernate.search.annotations.DynamicBoost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

/**
 * An embeddable object (or "component" in traditional Hibernate jargon), resenting a customer review for a software 
 * app.  A review consists of the username for the person submitting the review, the number of "stars" (out of a 
 * possible 5) the app was rated, and any comments from the reviewer.
 * 
 * The @Embeddable annotation tells Hibernate that the lifecycle of these objects is dependendant on that of their
 * containing entity.  In other words, when an app is deleted, all of its customer reviews will be deleted also.
 * 
 * Because this embeddable class is NOT annotated with @Indexed, Hibernate Search will not create a Lucene index 
 * just for it.  Any information indexed for this class will be stored in the Lucene index of a containing entity (i.e. App).
 * 
 * The @AnalyzerDef annotation defines a custom analyzer, to be applied below to the "comments" property.  This 
 * analyzer strips HTML tags, and applies a couple of token filters to reduce noise.
 * 
 * The @DynamicBoost annotation dynamically sets a relevance weight for objects of this class at runtime, using 
 * the FiveStarBoostStrategy class.
 */
@Embeddable
@AnalyzerDef(
	name="customerReviewAnalyzer",
	charFilters={ @CharFilterDef(factory=HTMLStripCharFilterFactory.class) },
	tokenizer=@TokenizerDef(factory=StandardTokenizerFactory.class),
	filters={ 
		@TokenFilterDef(factory=StandardFilterFactory.class),
		@TokenFilterDef(factory=StopFilterFactory.class)
	}
)
@DynamicBoost(impl=FiveStarBoostStrategy.class)
public class CustomerReview {

	/**
	 * The username of the person submitting the review.  The @Column annotation is unnecessary for embeddable objects, 
	 * but the Hibernate Search @Field annotation is still used to map the field in the Lucene index for any containing 
	 * entity (e.g. App).
	 */	
	@Field
	private String username;
	
	/**
	 * The rating given, on a scale of five (e.g. "4 out of 5 stars").  The @NumericField annotation tells Hibernate Search to 
	 * index this field using a specialized Lucene data structure (rather than a string), to make sorting and range queries 
	 * more efficient.
	 */
	@Field
	@NumericField
	private int stars;
	
	/**
	 * Free-form comments about the software app.  The @Column annotation is unnecessary for embeddable objects, but the 
	 * Hibernate Search @Field annotation is still used to map the field in the Lucene index for any containing entity (e.g. App).
	 * 
	 * The @Analyzer annotation actually applies the custom analyzer defined above.
	 */	
	@Field
	@Analyzer(definition="customerReviewAnalyzer")
	private String comments;

	/**
	 * Default empty constructor.
	 */
	public CustomerReview() {
	}
	
	/**
	 * A convenience constructor for setting an instance's fields in one step.  
	 */
	public CustomerReview(String username, int stars, String comments) {
		this.username = username;
		this.stars = stars;
		this.comments = comments;
	}

	//
	// GETTERS AND SETTERS
	//
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
}
