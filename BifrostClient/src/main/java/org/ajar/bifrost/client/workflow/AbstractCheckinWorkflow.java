package org.ajar.bifrost.client.workflow;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.MappingPackageOperation;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.LocalFile;
import org.ajar.bifrost.core.model.data.StoredFile;
import org.ajar.bifrost.core.model.data.StoredMapping;

public abstract class AbstractCheckinWorkflow implements CheckinWorkflow {
    
    /**
     * Step 12 & 7B: Upload the files that need to be uploaded and store the results in the stored mapping.
     * @param files
     * @param wrapper
     * @return
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public boolean uploadStoredFiles(Map<LocalFile, String> files, BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	files.forEach((localFile, location) -> {
    		try {
    			StoredFile file = MappingPackageOperation.uploadNewLocalFile(localFile, location);
    			StoredFile old = wrapper.getStoredMapping().getFiles().parallelStream().filter(present -> present.getName().equals(file.getName())).findFirst().orElse(null);
				if(old != null) {
					old.setVersion(file.getVersion());
				} else {
					wrapper.getStoredMapping().getFiles().add(file);
				}
			} catch (IOException | OperationNotSupportedException e) {
				e.printStackTrace();
			}
    	});
    	return wrapper.getStoredMapping().getFiles().size() == wrapper.getLocalMapping().getFiles().size();
    }
    
    /**
     * Step 13 & 8B: Update the StoredMapping version in local storage.
     * @param wrapper
     * @param additions
     * @throws OperationNotSupportedException
     * @throws IOException
     */
    public void updateRemoteStoredMapping(BifrostPersistenceWrapper wrapper) throws OperationNotSupportedException, IOException {
    	StoredMapping storedMapping = wrapper.getStoredMapping();
    	storedMapping.setVersion(wrapper.getLocalMapping().getVersion());
    	wrapper.saveStoredMapping();
    }
}
