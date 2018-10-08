package org.ajar.bifrost.bind;

import org.ajar.bifrost.tracking.LocalFileLeaf;

public class XmlLocalLeafAdapter extends XmlLeafAdapter<LocalFileLeaf> {

	@Override
	protected LocalFileLeaf createBound() {
		return new LocalFileLeaf();
	}

}
