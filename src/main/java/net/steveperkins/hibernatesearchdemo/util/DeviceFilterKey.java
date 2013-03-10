package net.steveperkins.hibernatesearchdemo.util;

import org.hibernate.search.filter.FilterKey;

/**
 * A custom FilterKey used by the DeviceFilterFactory class, to distinguish between device name strings.  Implementations of 
 * FilterKey must implement "equals()" and "hashCode()". 
 */
public class DeviceFilterKey extends FilterKey {

	private String deviceName;
	
	@Override
	public boolean equals(Object otherKey) {
		if(this.deviceName == null || !(otherKey instanceof DeviceFilterKey)) {
			return false;
		}
		DeviceFilterKey otherDeviceFilterKey = (DeviceFilterKey) otherKey;
		return otherDeviceFilterKey.deviceName != null && this.deviceName.equals(otherDeviceFilterKey.deviceName);
	}

	@Override
	public int hashCode() {
		if(this.deviceName == null) {
			return 0;
		}
		return this.deviceName.hashCode();
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}
