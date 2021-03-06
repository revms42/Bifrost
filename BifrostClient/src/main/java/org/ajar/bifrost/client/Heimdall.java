package org.ajar.bifrost.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.comm.BifrostClient;
import org.ajar.bifrost.client.comm.PersistenceMappingWrapper;
import org.ajar.bifrost.client.comm.impl.FileSystemPersistence;
import org.ajar.bifrost.client.model.BifrostPersistenceMapping;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.client.model.MappingInventory;
import org.ajar.bifrost.client.monitor.ChangeMonitor;
import org.ajar.bifrost.client.monitor.Monitor;
import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.LocalMapping;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.core.model.data.StoredMapping;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Main class for the Bifrost stand-alone demo client.
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class Heimdall {
	
	private static MappingInventory inventory;
	private static Map<String, BifrostPersistenceWrapper> mappings;
	private static BifrostClient bifrostClient;
	private static final Properties properties = new Properties();
	
	private static final Map<BifrostPersistenceWrapper, List<ChangeMonitor>> monitor = new HashMap<>();
    
    /**
     * Step 1: Get the configuration properties.
     * @param reader
     * @throws IOException
     */
    public static void loadProperties(Reader reader) throws IOException {
    	properties.load(reader);
    }
    
    public static void loadPropertiesFromFile(File location) throws IOException {
    	loadProperties(new FileReader(location));
    }
    
    public static void saveProperties(Writer writer) throws IOException {
    	properties.store(writer, "Heimdall " + (new Date()).toString());
    }
    
    public static void savePropertiesToFile(File location) throws IOException {
    	saveProperties(new FileWriter(location));
    }
    
    public static Properties getProperties() {
    	return properties;
    }
    
    /**
     * Step 2: Configure things based on the properties.
     * @param properties
     */
    public static void configureFromProperties(Properties properties) {
    	MappedFileOperations.getRegisteredClients().forEach(client -> {
    		client.configurationKeys().forEach(key -> {
    			if(properties.containsKey(key)) {
    				client.setConfigurationOption(key, properties.getProperty(key));
    			}
    		});
    		client.loadConfiguration();
    	});
    	
    	bifrostClient = BifrostClient.forServer(properties.getProperty(BifrostClient.KEY_SERVER, "http://localhost"));
    	
    	if(bifrostClient != null) {
        	bifrostClient.getConfigurationKeys().forEach(key -> {
        		if(properties.containsKey(key)) {
        			bifrostClient.setConfigurationSetting(key, properties.getProperty(key));
        		}
        	});
        	bifrostClient.initializeClient();
    	}
    }
    
    /**
     * Step 3: Load up your inventory mapping.
     * @param reader
     * @throws IOException
     */
    public static void loadMappingInventory() throws IOException {
    	String location = properties.getProperty(MappingInventory.INVENTORY_KEY);
    	
    	if(location == null) {
			try {
				File heimdall = new File(Heimdall.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				
	    		if(!heimdall.isDirectory()) heimdall = heimdall.getParentFile();
	    		
	    		location = heimdall.toPath().toAbsolutePath().resolve(new File("inventory.json").toPath()).toString();
	    		
	    		properties.setProperty(MappingInventory.INVENTORY_KEY, location);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
    	}
    	
    	File inventoryFile = new File(location);
    	
    	if(!inventoryFile.exists()) {
    		if(!inventoryFile.createNewFile()) {
    			throw new IOException("Cannot create new inventory file at location " + inventoryFile.getAbsolutePath());
    		}
    	}
    	
    	inventory = PersistenceMappingWrapper.gson.fromJson(new FileReader(inventoryFile), MappingInventory.class);
    	
    	if(inventory != null) {
        	mappings = inventory.getInventory().parallelStream().map(bifrostMapping -> {
        		return new BifrostPersistenceWrapper(bifrostMapping);
        	}).filter(localMapping -> localMapping != null).collect(toMap(wrapper -> wrapper.getName(), wrapper -> wrapper));
        	
        	loadMonitorInventory();
    	} else {
    		inventory = new MappingInventory(new LinkedList<>());
    		mappings = new HashMap<>();
    	}
    }
    
    public static Map<String, BifrostPersistenceWrapper> getInventory() {
    	return mappings;
    }
    
    /**
     * Before Step 5: Deal with the added StoredFiles from Step 4 and update the version of the in-memory StoredMapping version.
     * Step 5: Set up monitors for the given wrapper.
     * @param wrapper
     * @return any LocalFiles that cannot be monitored by the monitor directories listed.
     * @throws IOException 
     * @throws OperationNotSupportedException 
     */
    public static List<File> startMonitor(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
		//TODO: At the moment we can only monitor local file system files.
		List<File> dirs = wrapper.getLocalMapping().getMonitorLocations().parallelStream().map(
				location -> FileSystemPersistence.fromLocation(location)
		).collect(toList());
		List<ChangeMonitor> monitors = ChangeMonitor.createMonitors(dirs);
		monitors.forEach(monitor -> {
			try {
				monitor.filter(wrapper.getLocalMapping());
			} catch (OperationNotSupportedException | IOException e) {
				e.printStackTrace();
			}
		});
		
		List<File> allFiles = wrapper.getLocalMapping().getFiles().parallelStream().map(localFile -> FileSystemPersistence.fromLocation(localFile.getLocation())).collect(toList());
		List<File> monitoredFiles = monitors.parallelStream().flatMap(monitor -> { 
			return monitor.getWatchMap().keySet().parallelStream();
		}).map(location -> {
			return FileSystemPersistence.fromLocation(location);
		}).collect(toList());
		
		allFiles.removeAll(monitoredFiles);
		
		if(monitors.size() == dirs.size()) {
			if(allFiles.size() == 0) {
				monitor.put(wrapper, monitors);
			}
    		saveMonitorInventory();
			
    		return allFiles;
		} else {
			throw new IOException("Couldn't create monitors for all monitored directories!");
		}
    }
    
    public static BifrostClient getClient() {
    	return bifrostClient;
    }
    
    public static boolean isMonitoring(BifrostPersistenceWrapper wrapper) {
    	return monitor.containsKey(wrapper);
    }
    
    /**
     * Step 6: Stop the monitoring and add the changes into the associated buckets in the wrapper.
     * @param wrapper
     * @throws IOException 
     */
    public static void stopMonitor(BifrostPersistenceWrapper wrapper) throws IOException {
    	List<ChangeMonitor> monitors = monitor.get(wrapper);
    	
    	if(monitors != null && !monitors.isEmpty()) {
        	monitors.forEach(monitor -> {
        		boolean[] changes = monitor.computeChanges();
        		
    			try {
    				LocalMapping mapping = wrapper.getLocalMapping();
    	    		if(changes[0]) {
    	    			addLocalFileChanges(monitor.getModifications(), mapping, wrapper.getChangedLocalFiles());
    	    		}
    	    		if(changes[1]) {
    	    			addLocalFileChanges(monitor.getAdditions(), mapping, wrapper.getAddedLocalFiles());
    	    		}
    	    		if(changes[2]) {
    	    			addLocalFileChanges(monitor.getRemovals(), mapping, wrapper.getRemovedLocalFiles());
    	    		}
    			} catch (OperationNotSupportedException | IOException e) {
    				e.printStackTrace();
    			}
        	});
        	deleteMonitorFiles(wrapper);
        	monitor.remove(wrapper);
    	}
    }
    
    private static void addLocalFileChanges(List<File> files, LocalMapping mapping, List<LocalFile> list) {
    	List<String> modifiedLocations = files.parallelStream().map(file -> FileSystemPersistence.toLocation(file)).collect(toList());
    	
    	modifiedLocations.forEach(location -> {
    		LocalFile local = mapping.getFiles().parallelStream().filter(
    				localFile -> localFile.getLocation().equals(location)
    		).findFirst().orElse(null);
    		
    		list.add(local);
    	});
    }
    
    /**
     * Step 14: Notify the server that you're done with the given package.
     * @param wrapper
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public static boolean checkinPackage(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	StoredMapping storedMapping = wrapper.getStoredMapping();
    	RegisteredPackage returned = bifrostClient.updatePackage(storedMapping.getName(), wrapper.getRemoteLocation());
    	if(returned != null) {
        	if(returned.getVersion() != storedMapping.getVersion()) {
        		if(bifrostClient.setVersion(storedMapping.getName(), storedMapping.getVersion()) == null) {
        			return false;
        		}
        	}
        	wrapper.resetAccounting();
        	return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Step 4B: Create a new (mostly empty) wrapper with the local files you selected.
     * @param name
     * @param files
     * @param monitors
     * @return
     */
    public static BifrostPersistenceWrapper createNewMapping(String name, String localLocation, List<LocalFile> files, List<String> monitors) {
    	files.forEach(file -> file.setVersion(1L));
    	
    	BifrostPersistenceWrapper wrapper = new BifrostPersistenceWrapper(new BifrostPersistenceMapping(name, localLocation, ""));
    	LocalMapping localMapping = new LocalMapping(name, 0L, monitors, files);
    	wrapper.setLocalMapping(localMapping);
    	
    	StoredMapping storedMapping = new StoredMapping(name, 1L, new LinkedList<>());
    	wrapper.setStoredMapping(storedMapping);
    	
    	mappings.put(wrapper.getName(), wrapper);
    	
    	return wrapper;
    }
    
    private static File getConfigFileFromProperties(String property, String defaultName) throws IOException {
    	String location = properties.getProperty(property);
    	
    	if(location == null) {
			try {
				File heimdall = new File(Heimdall.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				
	    		if(!heimdall.isDirectory()) heimdall = heimdall.getParentFile();
	    		
	    		location = heimdall.toPath().toAbsolutePath().resolve(new File(defaultName).toPath()).toString();
	    		
	    		properties.setProperty(property, location);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
    	}
    	
    	return new File(location);
    }
    
    /**
     * Step 9B & 7C: Write the inventory to file.
     * @param writer
     * @throws IOException
     */
    public static void saveMappingInventory() throws IOException {
    	File inventoryFile = getConfigFileFromProperties(MappingInventory.INVENTORY_KEY, "inventory.mapping");
    	
    	if(!inventoryFile.exists()) {
    		if(!inventoryFile.createNewFile()) {
    			throw new IOException("Cannot create new inventory file at location " + inventoryFile.getAbsolutePath());
    		}
    	}
    	
    	List<BifrostPersistenceMapping> storageReady = mappings.values().parallelStream().map(wrapper -> new BifrostPersistenceMapping(wrapper)).collect(toList());
    	inventory.setInventory(storageReady);
    	String json = PersistenceMappingWrapper.gson.toJson(inventory);
    	
    	try(BufferedWriter bw = new BufferedWriter(new FileWriter(inventoryFile))) {
			bw.append(json);
		} catch (IOException e) {
			throw e;
		}
    }
    
    public static void saveMonitorInventory() throws IOException {
    	File monitorDir = getConfigFileFromProperties(Monitor.MONITOR_KEY, "monitors");
    	
    	if(!monitorDir.exists()) {
    		if(!monitorDir.mkdirs()) {
    			throw new IOException("Cannot create new monitor directory at location " + monitorDir.getAbsolutePath());
    		}
    	}
    	
    	monitor.forEach((wrapper, monitorList) -> {
    		for(int i = 0; i < monitorList.size(); i++) {
    			ChangeMonitor mon = monitorList.get(i);
        		File monitorFile = new File(monitorDir, wrapper.getName() + "-" + i + ".json");
        		
        		if(!monitorFile.exists()) {
        			try {
            			if(!monitorFile.createNewFile()) {
            				System.err.println("Cannot create new monitor file at location " + monitorFile.getAbsolutePath());
            			} else {
            				String json = PersistenceMappingWrapper.gson.toJson(mon);
            				
            		    	try(BufferedWriter bw = new BufferedWriter(new FileWriter(monitorFile))) {
            					bw.append(json);
            				} catch (IOException e) {
            					throw e;
            				}
            			}
        			} catch(IOException e) {
        				e.printStackTrace();
        			}
        		}
    		}
    	});
    }
    
    public static void loadMonitorInventory() throws IOException {
    	File monitorDir = getConfigFileFromProperties(Monitor.MONITOR_KEY, "monitors");
    	
    	if(monitorDir.exists() && monitorDir.isDirectory() && monitorDir.listFiles().length > 0) {
    		for(File file : monitorDir.listFiles()) {
    			String name = file.getName().split("-")[0];
    			
    			BifrostPersistenceWrapper selected = getInventory().get(name);
    			
    			if(selected != null) {
        			try(FileReader reader = new FileReader(file)) {
	       				 Monitor mon = PersistenceMappingWrapper.gson.fromJson(reader, Monitor.class);
	       				 
	       				 if(!monitor.containsKey(selected)) {
	       					 monitor.put(selected, new LinkedList<ChangeMonitor>());
	       				 }
	       				 monitor.get(selected).add(new ChangeMonitor(mon));
	       			}
    			}    			
    		}
    	}
    }
    
    public static void deleteMonitorFiles(BifrostPersistenceWrapper selected) throws IOException {
    	File monitorDir = getConfigFileFromProperties(Monitor.MONITOR_KEY, "monitors");
    	
    	if(monitorDir.exists() && monitorDir.isDirectory() && monitorDir.listFiles().length > 0) {
    		for(File file : monitorDir.listFiles()) {
    			String name = file.getName().split("-")[0];
    			
    			if(name.equals(selected.getName())) {
    				file.delete();
    			}    			
    		}
    	}
    	
    	if(monitorDir.listFiles().length == 0) {
    		monitorDir.delete();
    	}
    }
    
    /**
     * Step 6C: Save the local mapping to disk, add the wrapper to the inventory.
     * @param wrapper
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public static void persistLocalMapping(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	wrapper.saveLocalMapping();
    	mappings.put(wrapper.getName(), wrapper);
    }
    
    /**
     * Step 10 & 5B: Increment the local mapping and persist to local storage.
     * @param wrapper
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public static void persistLocalMappingChanges(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	LocalMapping mapping = wrapper.getLocalMapping();
    	mapping.setVersion(mapping.getVersion() + 1);
    	
    	wrapper.saveLocalMapping();
    	mappings.put(wrapper.getName(), wrapper);
    }
}
