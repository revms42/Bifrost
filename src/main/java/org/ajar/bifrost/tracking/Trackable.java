package org.ajar.bifrost.tracking;

import javax.xml.bind.annotation.XmlAttribute;

public interface Trackable {

	@XmlAttribute
	public long getVersion();
	public void setVersion(long version);
}
