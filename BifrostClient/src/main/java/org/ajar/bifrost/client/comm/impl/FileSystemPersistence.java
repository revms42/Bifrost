package org.ajar.bifrost.client.comm.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.comm.PersistenceClient;
import org.ajar.bifrost.client.comm.ResourceNotAvailableException;

public class FileSystemPersistence implements PersistenceClient {

	public static final String PROTOCOL_PREFIX = "file://";
	
	private static String stripPrefix(String location) {
		return location.replace(PROTOCOL_PREFIX, "");
	}
	
	private static String addPrefix(String location) {
		return PROTOCOL_PREFIX + location;
	}
	
	public static File fromLocation(String location) {
		return new File(stripPrefix(location));
	}
	
	public static String toLocation(File file) {
		return addPrefix(file.getPath());
	}
	
	@Override
	public String prefixLocation(String location) {
		return addPrefix(location);
	}
	
	@Override
	public String trimProtocol(String location) {
		return stripPrefix(location);
	}
	
	@Override
	public boolean canHandle(String location) {
		return location.startsWith(PROTOCOL_PREFIX);
	}

	@Override
	public boolean readFromRemote(OutputStream streamOut, String location) throws IOException, OperationNotSupportedException {
		try(BufferedInputStream input = readStreamFromLocal(location)) {
			input.transferTo(streamOut);
		} catch (FileNotFoundException e) {
			throw new ResourceNotAvailableException(location + " could not be found!");
		}
		return true;
	}

	@Override
	public boolean writeToRemote(InputStream streamIn, String location) throws IOException, OperationNotSupportedException {
		try(BufferedOutputStream output = writeStreamToLocal(location)) {
			streamIn.transferTo(output);
		} catch (FileNotFoundException e) {
			throw new ResourceNotAvailableException(location + " could not be found!");
		}
		return true;
	}

	@Override
	public BufferedInputStream readStreamFromLocal(String location) throws OperationNotSupportedException, IOException {
		File file = fromLocation(location);
		if(!file.exists()) {
			throw new ResourceNotAvailableException(location + " does not exist!");
		}
		return new BufferedInputStream(new FileInputStream(file));
	}

	@Override
	public BufferedOutputStream writeStreamToLocal(String location) throws OperationNotSupportedException, IOException {
		File file = fromLocation(location);
		if(!file.exists()) {
			throw new ResourceNotAvailableException(location + " does not exist!");
		}
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	@Override
	public void deleteFromLocal(String location) throws OperationNotSupportedException, IOException {
		File file = fromLocation(location);
		if(file.exists()) {
			file.delete();
		}
	}

	@Override
	public void deleteFromRemote(String location) throws OperationNotSupportedException, IOException {
		File file = fromLocation(location);
		if(file.exists()) {
			file.delete();
		}
	}
}
