package org.ajar.bifrost.network;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source")
public class Source {

	public enum SourceType {
		HTTP,
		DROPBOX,
		LOCAL;
	}
	
	private String location;
	private SourceType type;
	
	@XmlAttribute
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	@XmlAttribute
	public SourceType getProtocol() {
		return type;
	}
	
	public void setProtocol(SourceType type) {
		this.type = type;
	}
}
