package org.ajar.bifrost.core.model.data;

import java.util.List;

public class MappingPackage<T extends MappedFile> {

	private String name;
	private long version;
	private List<T> files;
	
	public MappingPackage(String name, long version, List<T> files) {
		this.files = files;
		this.name = name;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	
	public long getVersion() {
		return version;
	}
	
	public List<T> getFiles() {
		return files;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public void setFiles(List<T> files) {
		this.files = files;
	}
}
