package org.ajar.bifrost.core.model.data;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public interface RegisteredPackage extends PackageStatusDescription {

	long getVersion();

	String getLocation();

	void setVersion(long version);

	void setLocation(String location);
	
	default void copy(RegisteredPackage mapping) {
		setName(mapping.getName());
		setVersion(mapping.getVersion());
		setLocation(mapping.getLocation());
		setActive(mapping.isActive());
	}
}