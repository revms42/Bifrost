package org.ajar.bifrost.core.model.data;

import java.util.LinkedList;
import java.util.List;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class StoredMapping extends MappingPackage<StoredFile> {
	
	public StoredMapping(String name) {
		super(name, 0, new LinkedList<StoredFile>());
	}

	public StoredMapping(String name, long version, List<StoredFile> files) {
		super(name, version, files);
	}
}
