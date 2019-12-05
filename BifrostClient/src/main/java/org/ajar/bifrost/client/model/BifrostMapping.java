package org.ajar.bifrost.client.model;

public interface BifrostMapping {

	public String getName();

	public String getLocalLocation();

	public String getRemoteLocation();
	
	public void setName(String name);
	
	public void setLocalLocation(String localLocation);
	
	public void setRemoteLocation(String remoteLocation);
}