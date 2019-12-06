package org.ajar.bifrost.client.monitor;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ajar.bifrost.client.comm.impl.FileSystemPersistence;
import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.LocalMapping;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class ChangeMonitor extends Monitor {

	private final List<File> changed;
	private final List<File> added;
	private final List<File> removed;
	private final List<File> filtered;
	
	public ChangeMonitor(File file) {
		super(file, new HashMap<>());
		
		register(file);
		
		changed = new LinkedList<>();
		added = new LinkedList<>();
		removed = new LinkedList<>();
		filtered = new LinkedList<>();
	}
	
	public ChangeMonitor(Monitor monitor) {
		super(monitor.getRoot(), monitor.getWatchMap());
		
		changed = new LinkedList<>();
		added = new LinkedList<>();
		removed = new LinkedList<>();
		filtered = new LinkedList<>();
	}
	
	private void register(File file) {
		if(file.isDirectory()) {
			for(File child : file.listFiles()) {
				register(child);
			}
		} else {
			getWatchMap().put(FileSystemPersistence.toLocation(file), file.lastModified());
		}
	}
	
	private void changes(File file, Map<String,Long> touchTimes) {
		if(file.isDirectory()) {
			for(File child : file.listFiles()) {
				changes(child, touchTimes);
			}
		} else {
			String fileString = FileSystemPersistence.toLocation(file);
			if(touchTimes.containsKey(fileString)) {
				// This is a file we care about monitoring.
				if(touchTimes.get(fileString) < file.lastModified()) {
					// It has a new change time, it's changed.
					changed.add(file);
				}
				// We've found it, don't search for it any more.
				touchTimes.remove(fileString);
			} else if(!filtered.contains(file)) {
				// This is a file that isn't on the list of files that we definitely don't care about. It's added.
				added.add(file);
			}
		}
		removed.addAll(touchTimes.keySet().parallelStream().map(location -> {
			return FileSystemPersistence.fromLocation(location);
		}).collect(toList()));
	}
	
	public void filter(LocalMapping mapping) {
		HashMap<String, Long> filteredMap = new HashMap<>();
		for(LocalFile file : mapping.getFiles()) {
			String localFile = file.getLocation();
			
			// Iterate over the files we care about. If we find them in the watch map we add them to the ones we want to keep.
			if(getWatchMap().containsKey(localFile)) {
				filteredMap.put(localFile, getWatchMap().get(localFile));
			}
		}
		for(String rawFile : getWatchMap().keySet()) {
			// Iterate over all the files. If we don't have it in the filtered map, we add it to the filtered list.
			if(!filteredMap.containsKey(rawFile)) {
				filtered.add(FileSystemPersistence.fromLocation(rawFile));
			}
		}
		setWatchMap(filteredMap);
	}
	
	/**
	 * 
	 * @return array of [isChanged, hasAdded, hasRemoved]
	 */
	public boolean[] computeChanges() {
		added.clear();
		removed.clear();
		changed.clear();
		
		changes(getRoot(), new HashMap<>(getWatchMap()));
		
		return new boolean[]{ !changed.isEmpty(), !added.isEmpty(), !removed.isEmpty() };
	}
	
	public List<File> getAdditions() {
		return added;
	}
	
	public List<File> getRemovals() {
		return removed;
	}
	
	public List<File> getModifications() {
		return changed;
	}
	
	public Monitor toMonitor() {
		return new Monitor(getRoot(), getWatchMap());
	}
	
    public static List<ChangeMonitor> createMonitors(List<File> locations) {
    	return locations.parallelStream().map(location -> new ChangeMonitor(location)).collect(toList());
    }
}
