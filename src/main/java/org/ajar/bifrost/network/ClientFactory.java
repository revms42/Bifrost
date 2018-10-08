package org.ajar.bifrost.network;

public interface ClientFactory {

	public Client getClient(Source.SourceType type);
}
