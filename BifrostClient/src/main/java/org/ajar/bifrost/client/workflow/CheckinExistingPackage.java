package org.ajar.bifrost.client.workflow;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.LocalMapping;
import org.ajar.bifrost.core.model.data.RegisteredPackage;

/**
 x You stop playing some game.
 * - You request that the monitors give you the changes to the files. (stopMonitor)
 * - You update your LocalMapping for those files you already have. (updateLocalMappingForChanges)
 * > You request user input on what to do with the files that are new. (updateLocalMappingForAdditions)
 * > You delete from the LocalMapping any files that were removed (pending user input). (updateLocalMappingForDeletions)
 * - You update the version of your LocalMapping. (persistLocalMappingChanges)
 * - You persist the updated LocalMapping to local storage. (persistLocalMappingChanges)
 * - You update the local version of the StoredFiles in memory. (persistLocalFileChangesToStore)
 * - You persist the updated StoredFiles to remote storage. (persistLocalFileChangesToStore)
 * > You let the user map any new files that are being stored.
 * - You upload the newly mapped stored files. (uploadStoredFiles)
 * - You update the local version of the StoredMapping with any new files and all the versions (including its own).(updateRemoteStoredMapping)
 * - You persist the updated StoredMapping into remote storage. (updateRemoteStoredMapping)
 * - You let the server know that you've checked in the RegisteredPackage with it's location. (checkinPackage)
 * - You correct any version problem on the server based on your in-memory version of the StoredMapping. (checkinPackage
 */
public abstract class CheckinExistingPackage extends AbstractCheckinWorkflow {

	private final BifrostPersistenceWrapper wrapper;
	
	public CheckinExistingPackage(BifrostPersistenceWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	@Override
	public boolean startWorkflow() {
		try {
			Heimdall.stopMonitor(wrapper);
			
			boolean changed = !wrapper.getChangedLocalFiles().isEmpty();
			
			if(changed) {
				updateLocalMappingForChanges(wrapper);
			}
			
			List<LocalFile> newFiles = wrapper.getAddedLocalFiles();
			if(newFiles.size() > 0) {
				newFiles = this.askForNewFiles(newFiles);
				updateLocalMappingForAdditions(newFiles, wrapper);
			}
			
			List<LocalFile> deletedFiles = wrapper.getRemovedLocalFiles();
			if(deletedFiles.size() > 0) {
				deletedFiles = this.askForFileDeletions(deletedFiles);
				updateLocalMappingForDeletions(deletedFiles, wrapper);
			}
			
			boolean updatedFiles = changed || !newFiles.isEmpty() || !deletedFiles.isEmpty();
			
			if(updatedFiles) {
				Heimdall.persistLocalMappingChanges(wrapper);
			}
			
			Map<LocalFile, String> unmapped = persistLocalFileChangesToStore(wrapper);
			if(unmapped.size() > 0) {
				unmapped = askForRemoteMappings(unmapped);
				uploadStoredFiles(unmapped, wrapper);
			}
			
			updateRemoteStoredMapping(wrapper);
			
			if(Heimdall.checkinPackage(wrapper)) {
				RegisteredPackage info = Heimdall.getClient().getPackageInfo(wrapper.getName());
				
				if(info.getVersion() != wrapper.getLocalMapping().getVersion()) {
					Heimdall.getClient().setVersion(wrapper.getName(), wrapper.getLocalMapping().getVersion());
				}
				return true;
			} else {
				return false;
			}
		} catch (IOException | OperationNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public abstract List<LocalFile> askForNewFiles(List<LocalFile> files);
	public abstract List<LocalFile> askForFileDeletions(List<LocalFile> deletions);
	
    /**
     * Step 7: Take the list of changed local files and update their versions.
     * @param wrapper
     */
    public void updateLocalMappingForChanges(BifrostPersistenceWrapper wrapper) {
    	wrapper.getChangedLocalFiles().forEach(localFile -> localFile.setVersion(localFile.getVersion() + 1));
    	wrapper.setChangedLocalFiles(null);
    }
    
    /**
     * Step 8: Take the list of new local files that the user has vetted and add them to the mapping.
     * @param wrapper
     * @throws IOException 
     * @throws OperationNotSupportedException 
     */
    public void updateLocalMappingForAdditions(List<LocalFile> newFiles, BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	newFiles.forEach(file -> file.setVersion(1L));
    	
    	LocalMapping mapping = wrapper.getLocalMapping();
    	mapping.getFiles().addAll(newFiles);
    }
    
    /**
     * Step 9: Delete the list of things that the user wants you to delete.
     * @param wrapper
     * @throws IOException 
     * @throws OperationNotSupportedException 
     */
    public void updateLocalMappingForDeletions(List<LocalFile> deadFiles, BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	LocalMapping mapping = wrapper.getLocalMapping();
    	mapping.getFiles().removeAll(deadFiles);
    }
    
    /**
     * Step 11: Update the stored files that have changed. For local files with no mapping, return a map of the local file
     * with a proposed location.
     * @param wrapper
     * @return
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public Map<LocalFile, String> persistLocalFileChangesToStore(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	return wrapper.updateStoredFilesFromLocalMapping().parallelStream().collect(toMap(
    			localFile -> localFile,
    			localFile -> wrapper.getRemoteLocation() + "/" + wrapper.getName() + "/" + localFile.getName()
    	));
    }
}
