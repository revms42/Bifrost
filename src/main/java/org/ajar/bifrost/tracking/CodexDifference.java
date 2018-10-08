package org.ajar.bifrost.tracking;

import java.util.Set;

public interface CodexDifference {

	public Set<Leaf> changedLeaves();
	public Set<Leaf> addedLeaves();
	public Set<Leaf> removedLeaves();
	
	default boolean identical() {
		return changedLeaves().size() == 0 && addedLeaves().size() == 0 && removedLeaves().size() == 0;
	}
}
