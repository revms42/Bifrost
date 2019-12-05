package org.ajar.bifrost.client.workflow;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.LocalFile;

import static java.util.stream.Collectors.toMap;

/**
 * Second scenario:
 * < You have a remote server to talk to (configureFromProperties)
 * < You have an existing Bifrost mapping (local location, remote location). (loadMappingInventory)
 * * You want to start an upload of a new game.
 * - You select the folders you want to monitor and get a comprehensive list of files. (recurseForFiles)
 * > You select the files you care about from the list.
 * - You pass the files and the directories to a new bifrost mapping. (createNewMapping)
 * - You create the associated local mapping, and add all the files to it setting versions to 0. (createNewMapping)
 * > You ask the user where they want to save the local mapping.
 * - You persist the local mapping to local storage. (persistLocalMappingChanges)
 * > You prompt the user for where they want to store the stored mapping.
 * - You update the bifrost wrapper with the stored location. (createNewStoredFiles)
 * > You present the user with the list of files to be stored (default to all in the same place as the mapping).
 * - You upload the local files to the remote location specified and update the stored mapping. (uploadStoredFiles)
 * - You call the wrapper to persist the mapping to the stored location. (updateRemoteStoredMapping)
 * - You register the new content in the server. (checkinPackage)
 * - You save the existing BifrostMappingInventory. (saveMappingInventory)
 */
public abstract class CheckinNewPackage extends AbstractCheckinWorkflow {

	private final BifrostPersistenceWrapper wrapper;
	private String remoteLocation;
	
	public CheckinNewPackage(BifrostPersistenceWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	@Override
	public boolean startWorkflow() {
		try {
			remoteLocation = askForRemoteLocation();
			
			if(remoteLocation == null) return false;
			
			Map<LocalFile, String> needMapping = createNewStoredFiles(remoteLocation, wrapper);
			
			if(!uploadStoredFiles(needMapping, wrapper)) return false;
			
			wrapper.setRemoteLocation(remoteLocation + "/" + wrapper.getName() + ".json");
			
			updateRemoteStoredMapping(wrapper);
			
			if(!Heimdall.checkinPackage(wrapper)) return false;
			
			Heimdall.saveMappingInventory();
			
			return true;
		} catch (OperationNotSupportedException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public abstract String askForRemoteLocation();
    
    /**
     * Step 6B: Create stored files in the default location for 
     * the user to vet.
     * @param wrapper
     * @return
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public Map<LocalFile, String> createNewStoredFiles(String storedLocation, BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	List<LocalFile> localFiles = wrapper.getLocalMapping().getFiles();
    	
    	return localFiles.parallelStream().collect(toMap(
    			localFile -> localFile,
    			localFile -> storedLocation + "/" + wrapper.getName() + "/" + localFile.getName()
    	));
    }
}
