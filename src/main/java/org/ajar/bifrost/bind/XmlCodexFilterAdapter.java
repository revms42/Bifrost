package org.ajar.bifrost.bind;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.monitoring.CodexFilter;
import org.ajar.bifrost.monitoring.FilterGroup;
import org.ajar.bifrost.monitoring.PathFilter;

public class XmlCodexFilterAdapter extends XmlAdapter<XmlCodexFilterAdapter.XmlCodexFilter, CodexFilter> {

	@XmlRootElement(name = "codex-filter")
	public static class XmlCodexFilter {
		
		private Path path;
		private List<XmlCodexFilter> filters = new ArrayList<>();
		
		@XmlAttribute
		@XmlJavaTypeAdapter(XmlPathAdapter.class)
		public Path getPath() {
			return path;
		}
		
		public void setPath(Path path) {
			this.path = path;
		}
		
		@XmlElement(name = "entry")
		public List<XmlCodexFilter> getFilters() {
			return filters;
		}
		
		public void setFilter(List<XmlCodexFilter> filters) {
			this.filters = filters;
		}
	}

	@Override
	public CodexFilter unmarshal(XmlCodexFilter v) {
		if(v.getFilters() != null && v.getFilters().size() > 0) {
			return new FilterGroup(v.getFilters().stream().map(
					filter -> unmarshal(filter)
			).collect(Collectors.toSet()));
		} else {
			return new PathFilter(v.path);
		}
	}

	@Override
	public XmlCodexFilter marshal(CodexFilter v) {
		XmlCodexFilter xmlFilter = new XmlCodexFilter();
		if(v instanceof PathFilter) {
			xmlFilter.setPath(((PathFilter) v).getExclusion());
		} else if(v instanceof FilterGroup) {
			xmlFilter.setFilter(
					((FilterGroup) v).getFilters().stream().map(
							filter -> marshal(filter)
					).collect(Collectors.toList())
			);
		} else {
			throw new RuntimeException("Cannot find filter type " + v.getClass().getName());
		}
		
		return xmlFilter;
	}
}
