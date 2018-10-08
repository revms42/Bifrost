package org.ajar.bifrost.tracking;

import java.util.Set;

public interface Codex<L extends Leaf> extends Trackable {

	public String getTitle();

	public void setTitle(String title);

	public Set<L> getLeaves();

	public void setLeaves(Set<L> leaves);

}