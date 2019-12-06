package org.ajar.bifrost.client.comm.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.comm.PersistenceClient;
import org.ajar.bifrost.client.comm.ResourceNotAvailableException;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class StringPersistence implements PersistenceClient {

	public static final String PROTOCOL_PREFIX = "string://";
	
	@Override
	public boolean canHandle(String location) {
		return location.startsWith(PROTOCOL_PREFIX);
	}
	
	@Override
	public String prefixLocation(String location) {
		return toLocation(location);
	}
	
	@Override
	public String trimProtocol(String location) {
		return fromLocation(location);
	}
	
	public static String fromLocation(String location) {
		return location.replace(PROTOCOL_PREFIX, "");
	}
	
	public static String toLocation(String file) {
		return PROTOCOL_PREFIX + file;
	}

	/**
	 * We assume if you're using this method you're literally putting a string in with the json.
	 */
	@Override
	public ByteArrayInputStream readStreamFromLocal(String location) throws OperationNotSupportedException, IOException {
		return new ByteArrayInputStream(trimProtocol(location).getBytes());
	}

	@Override
	public ByteArrayOutputStream writeStreamToLocal(String location) throws OperationNotSupportedException, IOException {
		ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream();
		stringOutputStream.writeBytes(PROTOCOL_PREFIX.getBytes());
		return stringOutputStream;
	}

	/**
	 * We assume if you're using this method you're literally putting a string in with the json.
	 */
	@Override
	public boolean readFromRemote(OutputStream streamOut, String location) throws OperationNotSupportedException, IOException {
		try(ByteArrayInputStream input = readStreamFromLocal(location)) {
			input.transferTo(streamOut);
		} catch (FileNotFoundException e) {
			throw new ResourceNotAvailableException(location + " could not be found!");
		}
		return true;
	}

	@Override
	public boolean writeToRemote(InputStream streamIn, String location) throws OperationNotSupportedException, IOException {
		try(ByteArrayOutputStream output = writeStreamToLocal(location)) {
			streamIn.transferTo(output);
		} catch (FileNotFoundException e) {
			throw new ResourceNotAvailableException(location + " could not be found!");
		}
		return true;
	}

	@Override
	public void deleteFromLocal(String location) throws OperationNotSupportedException, IOException {
		throw new OperationNotSupportedException();
	}

	@Override
	public void deleteFromRemote(String location) throws OperationNotSupportedException, IOException {
		throw new OperationNotSupportedException();
	}
}
