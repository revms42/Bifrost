package org.ajar.bifrost.client.workflow;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.MappedFileOperations;
import org.ajar.bifrost.client.MappingPackageOperation;
import org.ajar.bifrost.client.comm.impl.FileSystemPersistence;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.LocalMapping;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.core.model.data.StoredFile;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public abstract class AbstractCheckoutWorkflow implements CheckoutWorkflow {

	protected final RegisteredPackage registeredPackage;
	private BifrostPersistenceWrapper wrapper;
	private Map<StoredFile, File> localFiles;
	private List<File> monitorLocations;
	
	public AbstractCheckoutWorkflow(RegisteredPackage registeredPackage) {
		this.registeredPackage = registeredPackage;
		this.localFiles = new HashMap<>();
	}
	
	@Override
	public boolean startWorkflow() {
		try {
			wrapper = getWrapper();
			
			if(wrapper == null) return false;
			
			wrapper.setAddedStoredFiles(wrapper.updateLocalFilesFromStoredMapping());
			
			if(wrapper.getLocalMapping().getVersion() < wrapper.getStoredMapping().getVersion()) {
				Map<StoredFile, File> proposedLocations = createNewLocalFiles(wrapper);
				
				if(proposedLocations.size() > 0) {
					localFiles.putAll(askForLocalFilePaths(proposedLocations));
					
					if(localFiles == null) return false;
					
					if(!downloadLocalFiles(localFiles, wrapper)) return false;
				}
			}
			
			Collection<File> unmonitored = null;
			while((unmonitored = Heimdall.startMonitor(wrapper)).size() > 0) {
				List<File> monitorProposals = proposeMonitorLocations(unmonitored);
				
				monitorLocations = askForMonitorLocations(monitorProposals);
				
				if(monitorLocations == null) return false;
				
				LocalMapping mapping = wrapper.getLocalMapping();
				List<String> oldLocations = mapping.getMonitorLocations();
				oldLocations.addAll(
						monitorLocations.parallelStream().map(file -> FileSystemPersistence.toLocation(file))
						.filter(string -> !oldLocations.contains(string)).collect(toList())
				);
			}

			wrapper.getLocalMapping().setVersion(wrapper.getStoredMapping().getVersion());
			
			Heimdall.persistLocalMapping(wrapper);
			Heimdall.saveMappingInventory();
			
			return true;
		} catch (OperationNotSupportedException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public abstract BifrostPersistenceWrapper getWrapper() throws OperationNotSupportedException, IOException;
	
    /**
     * Step 2C: Take the stored files and create a set of proposed locations for the local files.
     * the user to vet.
     * @param wrapper
     * @return
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public Map<StoredFile, File> createNewLocalFiles(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	List<StoredFile> storedFiles = wrapper.getAddedStoredFiles();
    	
		File location = FileSystemPersistence.fromLocation(wrapper.getLocalLocation());
		if(location.isDirectory()) {
			wrapper.setLocalLocation(FileSystemPersistence.toLocation(new File(location, wrapper.getName() + ".json")));
		} else {
			location = location.getParentFile();
		}
		
		final File finalLocation = location;
    	
    	return storedFiles.parallelStream().collect(toMap(
    			storedFile -> storedFile,
    			storedFile -> FileSystemPersistence.fromLocation(finalLocation.getAbsolutePath() + "/" + wrapper.getName() + "/" + storedFile.getName())
    	));
    }
    
    /**
     * Step 3C: Download the files that need to be downloaded and store the results in the local mapping.
     * @param files
     * @param wrapper
     * @return
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public boolean downloadLocalFiles(Map<StoredFile, File> files, BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	files.forEach((storedFile, location) -> {
    		try {
    			if(!location.getParentFile().exists()) location.getParentFile().mkdirs();
    			if(!location.exists()) location.createNewFile();
				wrapper.getLocalMapping().getFiles().add(MappingPackageOperation.downloadNewStoredFile(storedFile, FileSystemPersistence.toLocation(location)));
			} catch (IOException | OperationNotSupportedException e) {
				e.printStackTrace();
			}
    	});
    	return wrapper.getStoredMapping().getFiles().size() == wrapper.getLocalMapping().getFiles().size();
    }
    
    public List<File> proposeMonitorLocations(Collection<File> unmapped) {
    	List<Path> localPath = unmapped.parallelStream().map(
    			file -> file.toPath()
    	).collect(toList());
    	
    	localPath = MappedFileOperations.proposeMonitorPaths(localPath);
    	
    	return localPath.parallelStream().map(
    			path -> path.toFile()
    	).collect(toList());
    }
}
