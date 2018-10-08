package org.ajar.bifrost.monitoring;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlCodexFilterAdapter;
import org.ajar.bifrost.network.Source;

public interface UsageBinding {

	public Source getRemoteLocation();
	
	@XmlJavaTypeAdapter(XmlCodexFilterAdapter.class)
	public CodexFilter getFilter();
}
