package org.ajar.bifrost.client.comm;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
@SuppressWarnings("serial")
public class ResourceNotAvailableException extends RuntimeException {
	
	public ResourceNotAvailableException(String location) {
		super(location);
	}
}
