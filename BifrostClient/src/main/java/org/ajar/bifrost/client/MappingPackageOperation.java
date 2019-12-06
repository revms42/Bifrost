package org.ajar.bifrost.client;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.model.BifrostPersistenceMapping;
import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.MappedFile;
import org.ajar.bifrost.core.model.data.MappingPackage;
import org.ajar.bifrost.core.model.data.StoredFile;

import java.io.IOException;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class MappingPackageOperation {
	
	/**
	 * You've got a missing stored file (i.e. it doesn't exist locally). Given the local location, download it.
	 * @param storedFile
	 * @param localLocation
	 * @return the LocalFile entry for the newly downloaded file.
	 * @throws IOException
	 * @throws OperationNotSupportedException 
	 */
	public static LocalFile downloadNewStoredFile(StoredFile storedFile, String localLocation) throws IOException, OperationNotSupportedException {
		if(MappedFileOperations.downloadRemoteToLocal(new BifrostPersistenceMapping(storedFile.getName(), localLocation, storedFile.getLocation()))) {
			return new LocalFile(storedFile.getName(), localLocation, storedFile.getVersion());
		} else {
			throw new IOException("Couldn't download " + storedFile.getLocation() + " to " + localLocation); 
		}
	}
	
	/**
	 * The remote mapping is missing a file. Given the new remote location you want it stored, store it.
	 * @param localFile
	 * @param remoteLocation
	 * @return the StoredFile entry of the newly uploaded file.
	 * @throws IOException
	 * @throws OperationNotSupportedException 
	 */
	public static StoredFile uploadNewLocalFile(LocalFile localFile, String remoteLocation) throws IOException, OperationNotSupportedException {
		if(MappedFileOperations.publishLocalToRemote(new BifrostPersistenceMapping(localFile.getName(), localFile.getLocation(), remoteLocation))) {
			return new StoredFile(localFile.getName(), remoteLocation, localFile.getVersion());
		} else {
			throw new IOException("Couldn't upload " + localFile.getLocation() + " to " + remoteLocation); 
		}
	}
	
	public static <R extends MappedFile, L extends MappedFile> List<L> updateFirstToSecond(MappingPackage<R> first, MappingPackage<L> second) {
		ArrayList<L> needsUpdate = new ArrayList<>();
		
		List<R> currentVersion = first.getFiles();
		second.getFiles().forEach(file -> {
			R current = findInList(file, currentVersion); 
		    //R current = currentVersion.parallelStream().filter(original -> original.getName().equals(file.getName())).findFirst().orElse(null);
			
			if(current == null || current.getVersion() < file.getVersion()) {
				needsUpdate.add(file);
			}
		});
		
		return needsUpdate;
	}
	
	public static <T extends MappedFile> T findInList(MappedFile file, List<T> list) {
		return list.parallelStream().filter(original -> original.getName().equals(file.getName())).findFirst().orElse(null);
	}
}
