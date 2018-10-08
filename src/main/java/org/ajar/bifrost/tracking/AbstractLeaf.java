package org.ajar.bifrost.tracking;

import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlPathAdapter;

public abstract class AbstractLeaf implements Leaf {
	private Path path;

	/* (non-Javadoc)
	 * @see org.ajar.bifrost.tracking.ILeaf#getRelativePath()
	 */
	@Override
	@XmlAttribute(name = "path")
	@XmlJavaTypeAdapter(XmlPathAdapter.class)
	public Path getRelativePath() {
		return path;
	}
	
	/* (non-Javadoc)
	 * @see org.ajar.bifrost.tracking.ILeaf#setRelativePath(java.nio.file.Path)
	 */
	@Override
	public void setRelativePath(Path p) {
		this.path = p;
	}
	
	@Override
	public int hashCode() {
		return getRelativePath() != null? getRelativePath().hashCode() : super.hashCode();
	}
	
	@Override
	public boolean equals(Object l) {
		if(AbstractLeaf.class.isAssignableFrom(l.getClass())) {
			return this.getRelativePath().equals(((Leaf) l).getRelativePath());
		} else {
			return super.equals(l);
		}
	}
}
