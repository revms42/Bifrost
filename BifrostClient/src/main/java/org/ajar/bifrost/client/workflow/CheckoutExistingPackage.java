package org.ajar.bifrost.client.workflow;

import java.io.IOException;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.RegisteredPackage;

/**
 * First scenario:
 * < You have a remote server to talk to (configureFromProperties)
 * < You have an existing Bifrost mapping (local location, remote location). (loadMappingInventory)
 * - You load both the existing and remote versions of the mappings. (initializeMapping)
 * - You connect to the server and get an updated version of the StoredMapping via a RegisteredPackage. (initializeMapping)
 * - You update your local files to match the updated stored mapping. (initializeMapping)
 * > You find a place for any new stored files to live.
 * > You update the version of your stored mapping.
 * - You start monitoring the locations for your selected mapping. (startMonitor)
 */
public abstract class CheckoutExistingPackage extends AbstractCheckoutWorkflow {
	
	public CheckoutExistingPackage(RegisteredPackage registeredPackage) {
		super(registeredPackage);
	}
	
	public BifrostPersistenceWrapper getWrapper() throws OperationNotSupportedException, IOException {
		BifrostPersistenceWrapper wrapper = initializeMapping(registeredPackage);
		
		if(wrapper == null) throw new IOException("Couldn't initialize mapping!");
		
		return wrapper;
	}
	
    /**
     * Step 4: Given a RegisteredPackage from the server, initialize the mapping associated with it.
     * @param wrapper
     * @return <code>null</code> if there is no current mapping.
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public BifrostPersistenceWrapper initializeMapping(RegisteredPackage serverMapping) throws OperationNotSupportedException, IOException {
    	BifrostPersistenceWrapper wrapper = Heimdall.getInventory().get(serverMapping.getName());
    	
    	if(wrapper != null) {
    		wrapper.setRemoteLocation(serverMapping.getLocation());
    		if(wrapper.getStoredMapping().getVersion() < serverMapping.getVersion()) {
    			wrapper.setAddedStoredFiles(wrapper.updateLocalFilesFromStoredMapping());
    		}
    	}
    	
    	return wrapper;
    }
}
