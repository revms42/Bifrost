package org.ajar.bifrost.core.model.data;

public class MappedFile {
	
	private long version;
	private String name;
	private String location;

	public MappedFile(String name, String location, long version) {
		this.version = version;
		this.location = location;
		this.name = name;
	}
	
	public long getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(name != null) {
			builder.append(name);
			builder.append(": ");
		}
		builder.append(location);
		if(version > 0L) {
			builder.append(" -> #");
			builder.append(version);
		}
		return builder.toString();
	}
}
