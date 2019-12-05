package org.ajar.bifrost.server.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.ajar.bifrost.core.model.data.RegisteredPackage;

@Entity
public class RegisteredPackageEntity implements RegisteredPackage {

	@Id
	private String name;
	private String location;
	private long version;
	private boolean active;
	
	protected RegisteredPackageEntity() {}
	
	public RegisteredPackageEntity(String name, String location, long version, boolean active) {
		this.location = location;
		this.name = name;
		this.active = active;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	public String getLocation() {
		return location;
	}
	public long getVersion() {
		return version;
	}
	public boolean isActive() {
		return active;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setVersion(long version) {
		this.version = version;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
