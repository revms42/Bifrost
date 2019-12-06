package org.ajar.bifrost.client.model;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public interface BifrostMapping {

	public String getName();

	public String getLocalLocation();

	public String getRemoteLocation();
	
	public void setName(String name);
	
	public void setLocalLocation(String localLocation);
	
	public void setRemoteLocation(String remoteLocation);
}