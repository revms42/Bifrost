package org.ajar.bifrost.client.workflow;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.comm.impl.FileSystemPersistence;
import org.ajar.bifrost.client.model.BifrostPersistenceMapping;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.LocalMapping;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.core.model.data.StoredMapping;

/**
 * Third scenario:
 * < You have a remote server to talk to (configureFromProperties)
 * < You have an existing Bifrost mapping (local location, remote location). (loadMappingInventory)
 * * You want to download a new game from a remote location.
 * > You ask where you want to save the LocalMapping.
 * - You take the selected RegisteredPackage and location and use it to make a BifrostPersistenceWrapper with a stored mapping. (createNewMapping-2)
 * - You update the stored mapping. (createNewMapping-2)
 * - You create a map of StoredFiles to proposed local locations. (createNewLocalFiles)
 * > You let the user modify the list.
 * - You take the modified list and download all the files to the proposed locations, updating the LocalMapping. (downloadLocalFiles)
 * - You try to take a stab and figuring out where the monitors should go. (proposeMonitorLocations)
 * > You get user input on the the monitor locations.
 * - You save the monitor locations. (setMonitorLoations)
 * - You save the LocalMapping. (persistLocalMapping)
 * - You save the BifrostMappingInventory. (saveMappingInventory)
 * > You specify the port you want to use.
 * > You checkout the game you want, passing in your port.
 * x You play some game w-^_^-w
 * >> You continue as if you'd updated in scenario 1.
 */

public abstract class CheckoutNewPackage extends AbstractCheckoutWorkflow {
	
	private final File localMappingPath;
	
	public CheckoutNewPackage(RegisteredPackage registeredPackage, File localMappingPath) throws IOException {
		super(registeredPackage);
		this.localMappingPath = localMappingPath;
		
		if(!localMappingPath.exists()) {
			localMappingPath.createNewFile();
		}
	}
	
	public BifrostPersistenceWrapper getWrapper() throws OperationNotSupportedException, IOException {
		BifrostPersistenceWrapper wrapper = createNewMapping(FileSystemPersistence.toLocation(localMappingPath), registeredPackage);
		
		if(wrapper == null) throw new IOException("Couldn't create a new mapping!");
		
		wrapper.setAddedStoredFiles(wrapper.getStoredMapping().getFiles());
		
		return wrapper;
	}
	
    /**
     * Step 1C; Create a new mapping from the registered package.
     * @param location
     * @param registeredPackage
     * @return
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public BifrostPersistenceWrapper createNewMapping(String location, RegisteredPackage registeredPackage) throws OperationNotSupportedException, IOException {
    	StoredMapping storedMapping = new StoredMapping(registeredPackage.getName(), registeredPackage.getVersion(), new LinkedList<>());
    	
    	BifrostPersistenceWrapper wrapper = new BifrostPersistenceWrapper(new BifrostPersistenceMapping(registeredPackage.getName(), location, registeredPackage.getLocation()));
    	wrapper.setStoredMapping(storedMapping);
    	wrapper.setLocalMapping(new LocalMapping(registeredPackage.getName(), -1L, new LinkedList<>(), new LinkedList<>()));
    	
    	wrapper.updateStoredMappingFromSource();
    	
    	return wrapper;
    }
}
