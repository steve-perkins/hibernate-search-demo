package net.steveperkins.hibernatesearchdemo.domain;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;

/**
 * A simple entity class representing a hardware device... with a name, a manufacturer name, and a collection of 
 * software apps supported on the device.
 * 
 * The @Entity annotation tells Hibernate to treat this class as an entity in its own right, with a lifecycle not
 * necessarily tied to any other entity class.  However, because this class is NOT annotated with @Indexed, Hibernate 
 * Search will not create a Lucene index just for it.  Any information indexed for this class will be stored in the 
 * Lucene index of a containing entity (i.e. App).
 */
@Entity
public class Device implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * A primary key configured for automatic generation, so there is no need to populate it when creating 
	 * new instances of this class.
	 */	
	@Id
    @GeneratedValue
    private Long id;
	
	/**
	 * The name of the device manufacturer.  The @Column annotation tells Hibernate to map this variable 
	 * to a database table column, while the @Field annotation tells Hibernate Search to map it as a searchable 
	 * field in the Lucene index.
	 */	
	@Column
	@Field
    private String manufacturer;
	
	/**
	 * The name of the device itself.  The @Column annotation tells Hibernate to map this variable 
	 * to a database table column, while the @Field annotation tells Hibernate Search to map it as a searchable 
	 * field in the Lucene index.
	 */	
	@Column
	@Field
    private String name;

	/**
	 * A collection of associated App entities, representing the software apps supported on this device.  
	 * 
	 * The @ManyToMany annotation tells Hibernate that an app can be supported on multiple devices, and a device 
	 * can support multiple apps.  
	 * 
	 *      Although the relationship between App and Device is bidirectional, App is intended to be the primary container
	 *      entity.  Here, the "mappedBy" element tells core Hibernate which App member variable (i.e. App.supportedDevices)
	 *      references this entity.
	 * 
	 *      The "fetch" and "cascade" elements are set the same as with App.supportedDevices, and for the same reasons.
	 *      See the source code comments in App.java for more detail.
	 * 
	 * The Hibernate Search @ContainedIn annotation serves as the counterpart to @IndexedEmbedded (applied 
	 * to App.supportedDevices).  This tells Hibernate Search to include information about contained devices in the 
	 * Lucene index for App.
	 * 
	 * The @JsonIgnore annotation is used by Jersey and Jackson, in the RESTful service for fetching an app's full 
	 * detail, to prevent circular dependencies when convering an "App" object to JSON.  Normally, "App.supportedDevices" 
	 * would convert into an array of "Device" objects, each of which have a "supportedApps" property converting 
	 * back to the same "App", and so on indefinitely.  With this annotation, "Device.supportedApps" simply isn't 
	 * converted... which breaks the circular dependency, and doesn't cause problems on the search results JSP anyway.
	 */
	@ManyToMany(mappedBy="supportedDevices", fetch=FetchType.EAGER, cascade = { CascadeType.ALL })
	@ContainedIn
	@JsonIgnore
	private Set<App> supportedApps;

	/**
	 * Default empty constructor.
	 */	
	public Device() {
	}
	
	/**
	 * A convenience constructor for setting an instance's fields in one step.  
	 */
	public Device(String manufacturer, String name, Set<App> supportedApps) {
		this.manufacturer = manufacturer;
		this.name = name;
		this.supportedApps = supportedApps;
	}
	
	//
	// GETTERS AND SETTERS
	//
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<App> getSupportedApps() {
		return supportedApps;
	}

	public void setSupportedApps(Set<App> supportedApps) {
		this.supportedApps = supportedApps;
	}

}
