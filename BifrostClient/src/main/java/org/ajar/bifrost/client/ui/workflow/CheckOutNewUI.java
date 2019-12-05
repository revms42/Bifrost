package org.ajar.bifrost.client.ui.workflow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.ajar.bifrost.client.workflow.CheckoutNewPackage;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.core.model.data.StoredFile;

public class CheckOutNewUI extends CheckoutNewPackage {

	public CheckOutNewUI(RegisteredPackage registeredPackage, File localMappingPath) throws IOException {
		super(registeredPackage, localMappingPath);
	}

	@Override
	public Map<StoredFile, File> askForLocalFilePaths(Map<StoredFile, File> proposedPaths) {
		return FileSelectionDialog.mapFiles(proposedPaths, "Select Save Locations");
	}

	@Override
	public List<File> askForMonitorLocations(List<File> proposedPaths) {
		return FileSelectionDialog.selectFilesFromFileSystem(
				"Select Monitor Locations", 
				JFileChooser.DIRECTORIES_ONLY, 
				false, 
				proposedPaths.toArray(new File[proposedPaths.size()])
		);
	}

}
