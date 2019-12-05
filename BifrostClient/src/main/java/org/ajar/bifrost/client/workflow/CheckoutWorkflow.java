package org.ajar.bifrost.client.workflow;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ajar.bifrost.core.model.data.StoredFile;

public interface CheckoutWorkflow extends HeimdallWorkflow {
	
	public Map<StoredFile, File> askForLocalFilePaths(Map<StoredFile, File> proposedPaths);
	public List<File> askForMonitorLocations(List<File> proposedPaths);

}
