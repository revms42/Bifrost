package org.ajar.bifrost.network;

import org.ajar.bifrost.tracking.Trackable;

public interface Client {

	public boolean isReachable(String location);
	public boolean connect(String location);
	public boolean disconnect();
	
	public <A extends Trackable> boolean send(A trackable);
	public <A extends Trackable> A retrieve(Class<A> type);
}
