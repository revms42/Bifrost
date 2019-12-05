package org.ajar.bifrost.core.model.call;

import com.google.api.client.util.Key;

/**
 * To checkout you call get passing this, where location is your IP.
 * To checkin you call post passing this, where location is the url it's stored.
 * To add a new package you call post as well.
 * To delete you call delete with this, location not needed.
 * @author revms42
 *
 */
public class RegisterPackage {
	
	@Key
	private String name;
	@Key
	private String location;
	
	public RegisterPackage(String name, String location) {
		this.name = name;
		this.location = location;
	}
	
	public String getName() {
		return name;
	}
	public String getLocation() {
		return location;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
