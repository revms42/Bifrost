package org.ajar.bifrost.monitoring;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlCodexFilterAdapter;

@XmlRootElement
public class FilterGroup implements CodexFilter {

	private final Set<CodexFilter> codexFilters;
	
	public FilterGroup(Collection<CodexFilter> codexFilters) {
		this.codexFilters = new HashSet<>();
		this.codexFilters.addAll(codexFilters);
	}
	
	public FilterGroup(CodexFilter... codexFilters) {
		this(Arrays.asList(codexFilters));
	}
	
	public FilterGroup() {
		codexFilters = new HashSet<>();
	}
	
	@XmlJavaTypeAdapter(XmlCodexFilterAdapter.class)
	public Set<CodexFilter> getFilters() {
		return codexFilters;
	}
	
	public void setFilters(Set<CodexFilter> filters) {
		this.codexFilters.addAll(filters);
	}
	
	public void addFilter(Path p) {
		addFilter(new PathFilter(p));
	}
	
	public void addFilter(CodexFilter c) {
		codexFilters.add(c);
	}
	
	public CodexFilter removeFilter(Path p) {
		CodexFilter c = findFilter(p);
		
		if(c != null && removeFilter(c)) {
			return c;
		} else {
			return null;
		}
	}
	
	public boolean removeFilter(CodexFilter c) {
		return codexFilters.remove(c);
	}
	
	private CodexFilter findFilter(Path p) {
		return codexFilters.stream()
				.filter(filter -> filter.matches(p))
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public boolean matches(Path p) {
		return findFilter(p) != null;
	}

	@Override
	public int hashCode() {
		int code = 0;
		for(CodexFilter filter : getFilters().stream().sorted((o1, o2) -> o1.hashCode() - o2.hashCode()).collect(Collectors.toList())) {
			code = filter.hashCode() ^ code;
		}
		
		return code;
	}
	
	@Override
	public boolean equals(Object o) {
		if(FilterGroup.class.isAssignableFrom(o.getClass())) {
			HashSet<CodexFilter> union = new HashSet<>();
			union.addAll(getFilters());
			union.addAll(((FilterGroup) o).getFilters());
			
			return union.size() == getFilters().size() &&
					union.size() == ((FilterGroup) o).getFilters().size();
		} else {
			return super.equals(o);
		}
	}
}
