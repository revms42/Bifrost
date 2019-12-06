package org.ajar.bifrost.client.workflow;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.comm.impl.FileSystemPersistence;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.client.monitor.ChangeMonitor;
import org.ajar.bifrost.core.model.data.LocalFile;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public abstract class AbstractCreateWorkflow implements CreateWorkflow {

	private String name;
	private File saveLocation;
	private List<File> monitorLocations;
	private List<LocalFile> trackedFiles;
	
	private BifrostPersistenceWrapper wrapper;
	
	@Override
	public boolean startWorkflow() {
		monitorLocations = askForMonitorLocations();
		
		if(monitorLocations == null) return false;
		
		List<LocalFile> foundFiles = recurseForFiles(monitorLocations);
		
		if(foundFiles == null) return false;
		
		trackedFiles = askForNewFiles(foundFiles);
		
		if(trackedFiles == null) return false;
		
		boolean nameGood = false;
		
		while(!nameGood) {
			name = askForName();
			
			if(Heimdall.getInventory().containsKey(name)) {
				if(!notifyDuplicateName()) return false;
			} else {
				nameGood = true;
			}
		}
		
		saveLocation = askForLocalSaveLocation();
		
		if(saveLocation == null) return false;
		if(!saveLocation.exists())
			try {
				saveLocation.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		
		List<String> stringMonitors = monitorLocations.parallelStream().map(file -> FileSystemPersistence.toLocation(file)).collect(toList());
		wrapper = Heimdall.createNewMapping(name, FileSystemPersistence.toLocation(saveLocation), trackedFiles, stringMonitors);
	
		try {
			Heimdall.persistLocalMappingChanges(wrapper);
			Heimdall.getInventory().put(wrapper.getName(), wrapper);
		} catch (OperationNotSupportedException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public BifrostPersistenceWrapper getCreatedMapping() {
		return wrapper;
	}
    
    /**
     * Step 3B: Dig up all the files in the area you want to monitor.
     * Also loads.
     * @param name
     * @param locations
     */
    public List<LocalFile> recurseForFiles(List<File> locations) {
    	List<ChangeMonitor> monitors = ChangeMonitor.createMonitors(locations);
    	
    	return monitors.parallelStream().flatMap(monitor -> {
    		Path monitorPath = monitor.getRoot().toPath();
    		return monitor.getWatchMap().keySet().parallelStream().map(location -> {
    			File file = FileSystemPersistence.fromLocation(location);
    			Path filePath = file.toPath();
    			Path relative = monitorPath.relativize(filePath);
    			return new LocalFile(relative.toString(), FileSystemPersistence.toLocation(file), 1L);
    		});
    	}).collect(toList());
    }
}
