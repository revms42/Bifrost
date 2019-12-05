package org.ajar.bifrost.client.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.naming.OperationNotSupportedException;

public interface PersistenceClient {
	
	public boolean canHandle(String location);
	public String prefixLocation(String location);
	public String trimProtocol(String location);
	
	public default List<String> configurationKeys() {
		return Collections.emptyList();
	}
	public default void setConfigurationOption(String key, String value) {}
	public default void loadConfiguration() {}

	public InputStream readStreamFromLocal(String location) throws OperationNotSupportedException, IOException;
	public OutputStream writeStreamToLocal(String location) throws OperationNotSupportedException, IOException;
	public void deleteFromLocal(String location) throws OperationNotSupportedException, IOException;
	
	public boolean readFromRemote(OutputStream streamOut, String location) throws OperationNotSupportedException, IOException;
	public boolean writeToRemote(InputStream streamIn, String location) throws OperationNotSupportedException, IOException;
	public void deleteFromRemote(String location) throws OperationNotSupportedException, IOException;
}
