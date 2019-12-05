package org.ajar.bifrost.client.model;

public class BifrostPersistenceMapping implements BifrostMapping {
	
	private String name;
	private String localLocation;
	private String remoteLocation;
	
	public BifrostPersistenceMapping() {
		name = "";
		localLocation = "";
		remoteLocation = "";
	}
	
	public BifrostPersistenceMapping(BifrostMapping mapping) {
		this.name = mapping.getName();
		this.localLocation = mapping.getLocalLocation();
		this.remoteLocation = mapping.getRemoteLocation();
	}
	
	public BifrostPersistenceMapping(String name, String localLocation, String remoteLocation) {
		this.name = name;
		this.localLocation = localLocation;
		this.remoteLocation = remoteLocation;
	}
	
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getLocalLocation() {
		return localLocation;
	}
	@Override
	public String getRemoteLocation() {
		return remoteLocation;
	}
	@Override
	public void setLocalLocation(String localMapping) {
		this.localLocation = localMapping;
	}
	@Override
	public void setRemoteLocation(String remoteMapping) {
		this.remoteLocation = remoteMapping;
	}
	
}
