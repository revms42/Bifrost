package org.ajar.bifrost.client.comm;

import java.io.IOException;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.core.model.data.MappedFile;
import org.ajar.bifrost.core.model.data.MappingPackage;

import com.google.gson.Gson;

public interface PersistenceMappingWrapper<F extends MappedFile, T extends MappingPackage<F>> {
	
	public static Gson gson = new Gson();
	
	public T loadMapping(String location) throws IOException, OperationNotSupportedException;
	public void writeMapping(String location, T mapping) throws IOException, OperationNotSupportedException;
	public void deleteMapping(String location) throws IOException, OperationNotSupportedException;
}
