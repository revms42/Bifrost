package org.ajar.bifrost.network;

public interface Source {

	public enum SourceType {
		HTTP,
		DROPBOX,
		LOCAL;
	}
	public String location();
}
