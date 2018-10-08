package org.ajar.bifrost.bind;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "trackable")
public class XmlTrackableRepresentation {

	private long version;

	@XmlAttribute
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
