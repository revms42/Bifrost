package org.ajar.bifrost.core.model.data;

public interface PackageStatusDescription {

	String getName();
	boolean isActive();

	void setName(String name);
	void setActive(boolean active);
}
