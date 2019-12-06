package org.ajar.bifrost.server.model;

import java.util.LinkedList;
import java.util.List;

import org.ajar.bifrost.core.model.data.DefaultRegisteredPackage;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class RegisteredPackageList {

	private final List<DefaultRegisteredPackage> registered;
	
	public RegisteredPackageList() {
		registered = new LinkedList<>();
	}
	
	public List<DefaultRegisteredPackage> getRegistered() {
		return registered;
	}
}
