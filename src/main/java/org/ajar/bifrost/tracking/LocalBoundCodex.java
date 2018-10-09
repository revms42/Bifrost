package org.ajar.bifrost.tracking;

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlCodexFilterAdapter;
import org.ajar.bifrost.monitoring.CodexFilter;
import org.ajar.bifrost.monitoring.UsageBinding;
import org.ajar.bifrost.network.Source;

@XmlRootElement(name = "bound-codex")
public class LocalBoundCodex extends LocalFileCodex implements UsageBinding {

	private Source source;
	private CodexFilter filter;
	
	public LocalBoundCodex() {
		super();
	}
	
	public LocalBoundCodex(String string, File folder, long startVersion) {
		super(string, folder, startVersion);
	}

	@Override
	@XmlElement(name = "source")
	public Source getRemoteLocation() {
		return source;
	}
	
	public void setRemoteLocation(Source location) {
		this.source = location;
	}

	@Override
	@XmlElement(name = "filter")
	@XmlJavaTypeAdapter(XmlCodexFilterAdapter.class)
	public CodexFilter getFilter() {
		return filter;
	}
	
	public void setFilter(CodexFilter filter) {
		this.filter = filter;
	}

}
