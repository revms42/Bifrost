package org.ajar.bifrost.monitoring;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.ajar.bifrost.network.Source;

@XmlRootElement
public interface Library {

	public Source getLibraryAddess();
	public Set<UsageBinding> getCodices();
}
