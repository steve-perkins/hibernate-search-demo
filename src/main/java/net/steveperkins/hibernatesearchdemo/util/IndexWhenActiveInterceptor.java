package net.steveperkins.hibernatesearchdemo.util;

import net.steveperkins.hibernatesearchdemo.domain.App;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;

/**
 * This is an EntityIndexingInterceptor for the App entity, and is used for conditional indexing.  The 
 * interceptor is intended to prevent indexing of "inactive" software apps (i.e. App instances where the 
 * "active" member variable is set to "false").  
 * 
 * The @Indexed annotation on the App entity class registers this interceptor, and causes one of its methods 
 * to be invoked when something happens to an App instance at the core Hibernate level.  The relevant method 
 * then determines what should happen in the Lucene index at the Hibernate Search level.
 * 
 * NOTE: Conditional indexing is not completely reliable, because interceptors are ignored when Lucene is
 *       re-indexed manually.  This is a known issue, and may be corrected in future Hibernate Search versions.
 *       See Chapter 2 of the book for more detail.  
 */
public class IndexWhenActiveInterceptor implements EntityIndexingInterceptor<App> {
	
	/** 
	 * This method in automatically invoked when a new App is first persisted by core Hibernate.  When the 
	 * App is active, the default Hibernate Search operation (i.e. add to the Lucene index) is used.
	 * When the App is inactive, Lucene indexing is skipped. 
	 */
	public IndexingOverride onAdd(App entity) {
		if(entity.isActive()) {
			return IndexingOverride.APPLY_DEFAULT;
		}
		return IndexingOverride.SKIP;
	}

	/**
	 * This method is automatically invoked when a new App is deleted by core Hibernate.  Regardless of whether
	 * the App is active, this method tells Hibernate Search to use the default action (i.e. remove it from 
	 * the Lucene index if present).
	 */
	public IndexingOverride onDelete(App entity) {
		return IndexingOverride.APPLY_DEFAULT;
	}

	/**
	 * This method is automatically invoked when an existing App is updated by core Hibernate.  If the current 
	 * state of the App is active, then this method calls for an update in the Lucene index.  If the App is 
	 * now inactive, then this method calls for it to be removed from the Lucene index.
	 */
	public IndexingOverride onUpdate(App entity) {
		if(entity.isActive()) {
			return IndexingOverride.UPDATE;
		} else {
			return IndexingOverride.REMOVE;
		}
	}

	/**
	 * This method is automatically invoked when an existing App is part of a larger collection that is being 
	 * updated by core Hibernate.  This doesn't matter to the purpose of this interceptor, so this method simply 
	 * uses the regular "onUpdate() above".
	 */
	public IndexingOverride onCollectionUpdate(App entity) {
		return onUpdate(entity);
	}

}
