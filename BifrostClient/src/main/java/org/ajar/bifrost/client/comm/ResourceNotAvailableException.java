package org.ajar.bifrost.client.comm;

@SuppressWarnings("serial")
public class ResourceNotAvailableException extends RuntimeException {
	
	public ResourceNotAvailableException(String location) {
		super(location);
	}
}
