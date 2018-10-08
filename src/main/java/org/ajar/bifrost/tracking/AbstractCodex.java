package org.ajar.bifrost.tracking;

import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public abstract class AbstractCodex<L extends Leaf> implements Codex<L> {

	private String title;
	private Set<L> leaves;
	private long version;
	
	/* (non-Javadoc)
	 * @see org.ajar.bifrost.tracking.ICodex#getTitle()
	 */
	@Override
	@XmlAttribute
	public String getTitle() {
		return title;
	}
	
	/* (non-Javadoc)
	 * @see org.ajar.bifrost.tracking.ICodex#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	@XmlAttribute
	public long getVersion() {
		return this.version;
	}
	
	@Override
	public void setVersion(long version) {
		this.version = version;
	}
	
	/* (non-Javadoc)
	 * @see org.ajar.bifrost.tracking.Codex#getLeaves()
	 */
	@XmlTransient
	public Set<L> getLeaves() {
		return leaves;
	}
	
	/* (non-Javadoc)
	 * @see org.ajar.bifrost.tracking.Codex#setLeaves(java.util.Set)
	 */
	@Override
	public void setLeaves(Set<L> leaves) {
		this.leaves = leaves;
	}
	
	/**
	 * Describes the difference between this <code>Codex</code> and
	 * the provided <code>Codex</code>, which is assumed to be the
	 * original before changes were made.
	 * <p>
	 * Note: That this is not designed to handle changes from multiple
	 * sources. i.e. if you changed a file, and someone else changed a file
	 * then checked in that version, this would overwrite that checkin 
	 * because it would assume that the version that is remote was the
	 * version you started with.
	 * @param original
	 * @return
	 */
	public abstract CodexDifference describeDifference(Codex<?> original);
	
	@Override
	public int hashCode() {
		return getTitle() != null ? getTitle().hashCode() : super.hashCode();
	}
	
	@Override 
	public boolean equals(Object o) {
		if(AbstractCodex.class.isAssignableFrom(o.getClass())) {
			return (getTitle().equals(((Codex<?>) o).getTitle()));
		} else {
			return super.equals(o);
		}
	}
}
