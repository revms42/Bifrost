package org.ajar.bifrost.core.model.data;

import java.util.List;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class LocalMapping extends MappingPackage<LocalFile> {
	
	private List<String> monitorLocations;

	public LocalMapping(String name, long version, List<String> monitorLocations, List<LocalFile> files) {
		super(name, version, files);
		this.setMonitorLocations(monitorLocations);
	}

	public List<String> getMonitorLocations() {
		return monitorLocations;
	}

	public void setMonitorLocations(List<String> monitorLocations) {
		this.monitorLocations = monitorLocations;
	}
}
