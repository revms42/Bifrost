package org.ajar.bifrost.client.model;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.MappedFileOperations;
import org.ajar.bifrost.client.MappingPackageOperation;
import org.ajar.bifrost.client.comm.LocalMappingPersistenceWrapper;
import org.ajar.bifrost.client.comm.PersistenceClient;
import org.ajar.bifrost.client.comm.StoredMappingPersistenceWrapper;
import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.LocalMapping;
import org.ajar.bifrost.core.model.data.StoredFile;
import org.ajar.bifrost.core.model.data.StoredMapping;

public class BifrostPersistenceWrapper implements BifrostMapping {

	private final BifrostMapping mapping;
	
	private LocalMapping localMapping;
	private StoredMapping storedMapping;
	
	private LocalMappingPersistenceWrapper localWrapper;
	private StoredMappingPersistenceWrapper storedWrapper;
	
	private List<LocalFile> changedLocalFiles;
	private List<LocalFile> addedLocalFiles;
	private List<LocalFile> removedLocalFiles;
	
	private List<StoredFile> changedStoredFiles;
	private List<StoredFile> addedStoredFiles;
	private List<StoredFile> removedStoredFiles;
	
	public BifrostPersistenceWrapper(BifrostMapping mapping) {
		this.mapping = mapping;
	}

	public LocalMapping getLocalMapping() throws OperationNotSupportedException, IOException {
		if(localMapping == null) {
			if(getLocalLocation().length() > 0) {
				updateLocalMappingFromSource();
			}
		}
		return localMapping;
	}
	
	public void setLocalMapping(LocalMapping localMapping) {
		this.localMapping = localMapping;
		localWrapper = null;
	}
	
	public StoredMapping getStoredMapping() throws OperationNotSupportedException, IOException {
		if(storedMapping == null) {
			if(getRemoteLocation().length() > 0) {
				updateStoredMappingFromSource();
			} else {
				storedMapping = new StoredMapping(getLocalMapping().getName());
			}
		}
		return storedMapping;
	}
	
	public void setStoredMapping(StoredMapping storedMapping) {
		this.storedMapping = storedMapping;
		storedWrapper = null;
	}
	
	private LocalMappingPersistenceWrapper getLocalWrapper() {
		if(localWrapper == null) {
			PersistenceClient client = MappedFileOperations.findPersistenceClientForLocation(getLocalLocation());
			localWrapper = new LocalMappingPersistenceWrapper(client);
		}
		
		return localWrapper;
	}
	
	private StoredMappingPersistenceWrapper getStoredWrapper() {
		if(storedWrapper == null) {
			PersistenceClient client = MappedFileOperations.findPersistenceClientForLocation(getRemoteLocation());
			storedWrapper = new StoredMappingPersistenceWrapper(client);
		}
		
		return storedWrapper;
	}
	
	public void saveLocalMapping() throws OperationNotSupportedException, IOException {
		getLocalWrapper().writeMapping(getLocalLocation(), getLocalMapping());
	}
	
	public void saveStoredMapping() throws OperationNotSupportedException, IOException {
		getStoredWrapper().writeMapping(getRemoteLocation(), getStoredMapping());
	}
	
	public void updateLocalMappingFromSource() throws OperationNotSupportedException, IOException {
		setLocalMapping(getLocalWrapper().loadMapping(getLocalLocation()));
	}
	
	public void updateStoredMappingFromSource() throws OperationNotSupportedException, IOException {
		StoredMapping mapping = getStoredWrapper().loadMapping(getRemoteLocation());
		
		if(mapping == null) {
			LocalMapping local = getLocalMapping();
			mapping = new StoredMapping(local.getName(), -1L, new LinkedList<>());
		}
		
		setStoredMapping(mapping);
	}
	
	/**
	 * Returns a list of stored files that have no local equivalent.
	 * @return
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public List<StoredFile> updateLocalFilesFromStoredMapping() throws OperationNotSupportedException, IOException {
		LocalMapping localMapping = getLocalMapping();
		StoredMapping remoteMapping = getStoredMapping();
		
		List<StoredFile> needsUpdate = MappingPackageOperation.updateFirstToSecond(localMapping, remoteMapping);
		
		Map<LocalFile,Long> newVersions = new HashMap<>();
		List<BifrostMapping> updatable = localMapping.getFiles().parallelStream().map(localFile -> {
			StoredFile storeFile = MappingPackageOperation.findInList(localFile, needsUpdate);
			
			BifrostPersistenceMapping mapping = null;
			if(storeFile != null) {
				mapping = new BifrostPersistenceMapping(localFile.getName(), localFile.getLocation(), storeFile.getLocation());
				needsUpdate.remove(storeFile);
				newVersions.put(localFile, storeFile.getVersion());
			}
			return mapping;
		}).filter(mapping -> mapping != null).collect(toList());
		
		if(!MappedFileOperations.downloadRemoteToLocal(updatable)) {
			throw new IOException("Couldn't download all of the files specififed!");
		} else {
			newVersions.forEach((file, version) -> file.setVersion(version)); 
		}
		return needsUpdate;
	}
	
	/**
	 * Returns a list of local files that have no stored equivalent.
	 * @return
	 * @throws OperationNotSupportedException
	 * @throws IOException
	 */
	public List<LocalFile> updateStoredFilesFromLocalMapping() throws OperationNotSupportedException, IOException {
		LocalMapping localMapping = getLocalMapping();
		StoredMapping remoteMapping = getStoredMapping();
		
		List<LocalFile> needsUpdate = MappingPackageOperation.updateFirstToSecond(remoteMapping, localMapping);
		
		Map<StoredFile,Long> newVersions = new HashMap<>();
		List<BifrostMapping> updatable = remoteMapping.getFiles().parallelStream().map(remoteFile -> {
			LocalFile localFile = MappingPackageOperation.findInList(remoteFile, needsUpdate);
			
			BifrostPersistenceMapping mapping = null;
			if(localFile != null) {
				mapping = new BifrostPersistenceMapping(remoteFile.getName(), localFile.getLocation(), remoteFile.getLocation());
				needsUpdate.remove(localFile);
				newVersions.put(remoteFile, localFile.getVersion());
			}
			return mapping;
		}).filter(mapping -> mapping != null).collect(toList());
		
		if(!MappedFileOperations.publishLocalToRemote(updatable)) {
			throw new IOException("Couldn't upload all of the files specififed!");
		} else {
			newVersions.forEach((file, version) -> file.setVersion(version)); 
		}
		return needsUpdate;
	}

	@Override
	public String getName() {
		return mapping.getName();
	}

	@Override
	public String getLocalLocation() {
		return mapping.getLocalLocation() == null? "" : mapping.getLocalLocation();
	}

	@Override
	public String getRemoteLocation() {
		return mapping.getRemoteLocation() == null? "" : mapping.getRemoteLocation();
	}

	@Override
	public void setName(String name) {
		mapping.setName(name);
	}

	@Override
	public void setLocalLocation(String localLocation) {
		mapping.setLocalLocation(localLocation);
	}

	@Override
	public void setRemoteLocation(String remoteLocation) {
		mapping.setRemoteLocation(remoteLocation);
	}

	public List<LocalFile> getChangedLocalFiles() {
		if(changedLocalFiles == null) {
			changedLocalFiles = new LinkedList<>();
		}
		return changedLocalFiles;
	}

	public List<LocalFile> getAddedLocalFiles() {
		if(addedLocalFiles == null) {
			addedLocalFiles = new LinkedList<>();
		}
		return addedLocalFiles;
	}

	public List<LocalFile> getRemovedLocalFiles() {
		if(removedLocalFiles == null) {
			removedLocalFiles = new LinkedList<>();
		}
		return removedLocalFiles;
	}

	public void setChangedLocalFiles(List<LocalFile> changedLocalFiles) {
		this.changedLocalFiles = changedLocalFiles;
	}

	public void setAddedLocalFiles(List<LocalFile> addedLocalFiles) {
		this.addedLocalFiles = addedLocalFiles;
	}

	public void setRemovedLocalFiles(List<LocalFile> removedLocalFiles) {
		this.removedLocalFiles = removedLocalFiles;
	}

	public List<StoredFile> getChangedStoredFiles() {
		if(changedStoredFiles == null) {
			changedStoredFiles = new LinkedList<>();
		}
		return changedStoredFiles;
	}

	public List<StoredFile> getAddedStoredFiles() {
		if(addedStoredFiles == null) {
			addedStoredFiles = new LinkedList<>();
		}
		return addedStoredFiles;
	}

	public List<StoredFile> getRemovedStoredFiles() {
		if(removedStoredFiles == null) {
			removedStoredFiles = new LinkedList<>();
		}
		return removedStoredFiles;
	}

	public void setChangedStoredFiles(List<StoredFile> changedStoredFiles) {
		this.changedStoredFiles = changedStoredFiles;
	}

	public void setAddedStoredFiles(List<StoredFile> addedStoredFiles) {
		this.addedStoredFiles = addedStoredFiles;
	}

	public void setRemovedStoredFiles(List<StoredFile> removedStoredFiles) {
		this.removedStoredFiles = removedStoredFiles;
	}
	
	public void resetAccounting() {
		setChangedLocalFiles(null);
		setAddedLocalFiles(null);
		setRemovedLocalFiles(null);
		setChangedStoredFiles(null);
		setAddedStoredFiles(null);
		setRemovedStoredFiles(null);
	}
	
	public void deleteLocalMapping() throws OperationNotSupportedException, IOException {
		getLocalWrapper().deleteMapping(getLocalLocation());
	}
	
	public void deleteRemoteMapping() throws OperationNotSupportedException, IOException {
		getStoredWrapper().deleteMapping(getLocalLocation());
	}
}
