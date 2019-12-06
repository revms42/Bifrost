package org.ajar.bifrost.client.comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.core.model.data.StoredFile;
import org.ajar.bifrost.core.model.data.StoredMapping;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class StoredMappingPersistenceWrapper implements PersistenceMappingWrapper<StoredFile, StoredMapping> {
	
	private final PersistenceClient client;
	
	public StoredMappingPersistenceWrapper(PersistenceClient client) {
		this.client = client;
	}

	@Override
	public StoredMapping loadMapping(String location) throws OperationNotSupportedException, IOException {
		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		if(client.readFromRemote(streamOut, location)) {
			String all = new String(streamOut.toByteArray());
			
			return gson.fromJson(all, StoredMapping.class);
		} else {
			return null;
		}
	}
	
	@Override
	public void writeMapping(String location, StoredMapping mapping) throws OperationNotSupportedException, IOException {
		StringWriter writer = new StringWriter();
		gson.toJson(mapping, writer);
		ByteArrayInputStream streamIn = new ByteArrayInputStream(writer.toString().getBytes());
		
		client.writeToRemote(streamIn, location);
	}

	@Override
	public void deleteMapping(String location) throws IOException, OperationNotSupportedException {
		client.deleteFromRemote(location);
	}
}
