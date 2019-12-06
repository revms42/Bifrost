package org.ajar.bifrost.core.model.data;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public interface PackageStatusDescription {

	String getName();
	boolean isActive();

	void setName(String name);
	void setActive(boolean active);
}
