package org.ajar.bifrost.monitoring;

import java.nio.file.Path;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlPathAdapter;

@XmlRootElement
public class PathFilter implements CodexFilter {

	private Path path;
	
	public PathFilter() {}
	
	public PathFilter(Path path) {
		this.path = path;
	}
	
	@XmlJavaTypeAdapter(XmlPathAdapter.class)
	public Path getExclusion() {
		return path;
	}
	
	public void setExclusion(Path path) {
		this.path = path;
	}
	
	@Override
	public boolean equals(Object p) {
		if(PathFilter.class.isAssignableFrom(p.getClass())) {
			return this.path.equals(((PathFilter) p).getExclusion());	
		}else {
			return super.equals(p);
		}
	}
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
	public String toString() {
		return "!" + path.toString();
	}
	
	@Override
	public boolean matches(Path p) {
		return p.startsWith(path);
	}
}
