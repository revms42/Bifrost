package org.ajar.bifrost.bind;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.ajar.bifrost.tracking.Trackable;

public abstract class XmlTrackableAdapater<X extends XmlTrackableRepresentation, B extends Trackable> extends XmlAdapter<X, B> {

	protected abstract X createRepresentation();
	protected abstract B createBound();
	
	@Override
	public B unmarshal(X v) throws Exception {
		B b = createBound();
		b.setVersion(v.getVersion());
		return b;
	}

	@Override
	public X marshal(B v) throws Exception {
		X x = createRepresentation();
		x.setVersion(v.getVersion());
		return x;
	}

}
