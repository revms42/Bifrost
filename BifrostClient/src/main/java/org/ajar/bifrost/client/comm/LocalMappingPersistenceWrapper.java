package org.ajar.bifrost.client.comm;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.LocalMapping;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class LocalMappingPersistenceWrapper implements PersistenceMappingWrapper<LocalFile, LocalMapping> {
	
	private final PersistenceClient client;
	
	public LocalMappingPersistenceWrapper(PersistenceClient client) {
		this.client = client;
	}
	
	@Override
	public LocalMapping loadMapping(String location) throws IOException, OperationNotSupportedException {
		try(InputStreamReader reader = new InputStreamReader(client.readStreamFromLocal(location))) {
			return gson.fromJson(reader, LocalMapping.class);
		} catch (IOException | OperationNotSupportedException e) {
			throw e;
		} 
	}
	
	@Override
	public void writeMapping(String location, LocalMapping mapping) throws IOException, OperationNotSupportedException {
		try(OutputStreamWriter writer = new OutputStreamWriter(client.writeStreamToLocal(location))) {
			gson.toJson(mapping, writer);
		} catch (IOException | OperationNotSupportedException e) {
			throw e;
		}
	}

	@Override
	public void deleteMapping(String location) throws IOException, OperationNotSupportedException {
		client.deleteFromLocal(location);
	}
}
