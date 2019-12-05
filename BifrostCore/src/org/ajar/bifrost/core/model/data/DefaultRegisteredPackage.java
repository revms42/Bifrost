package org.ajar.bifrost.core.model.data;

import com.google.api.client.util.Key;

public class DefaultRegisteredPackage implements RegisteredPackage {

	@Key
	private String name;
	@Key
	private long version;
	@Key
	private String location;
	@Key
	private boolean active;
	
	public DefaultRegisteredPackage() {}
	
	public DefaultRegisteredPackage(String name, long version, String location, boolean active) {
		this.name = name;
		this.version = version;
		this.location = location;
		this.active = active;
	}
	
	@Override
	public String getName() {
		return name;
	}
	@Override
	public long getVersion() {
		return version;
	}
	@Override
	public String getLocation() {
		return location;
	}
	@Override
	public boolean isActive() {
		return active;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public void setVersion(long version) {
		this.version = version;
	}
	@Override
	public void setLocation(String location) {
		this.location = location;
	}
	@Override
	public void setActive(boolean active) {
		this.active = active;
	}
}
