package org.ajar.bifrost.bind;

import org.ajar.bifrost.tracking.LocalFileCodex;
import org.ajar.bifrost.tracking.LocalFileLeaf;

public class XmlLocalFileCodexAdapter extends XmlCodexAdapter<LocalFileLeaf, LocalFileCodex> {

	private final XmlLocalLeafAdapter adapter = new XmlLocalLeafAdapter();
	
	@Override
	protected LocalFileCodex createBound() {
		return new LocalFileCodex();
	}

	@Override
	protected XmlLeafAdapter<LocalFileLeaf> getLeafAdapter() {
		return adapter;
	}

}
