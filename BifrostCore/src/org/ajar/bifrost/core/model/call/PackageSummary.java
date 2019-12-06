package org.ajar.bifrost.core.model.call;

import org.ajar.bifrost.core.model.data.PackageStatusDescription;

import com.google.api.client.util.Key;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class PackageSummary implements PackageStatusDescription {
	
	@Key
	private String name;
	@Key
	private boolean active;
	
	public PackageSummary() {}
	
	public PackageSummary(String name, boolean active) {
		this.active = active;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public boolean isActive() {
		return active;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
