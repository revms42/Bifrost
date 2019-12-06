package org.ajar.bifrost.client.ui.workflow;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.ajar.bifrost.client.workflow.CheckoutExistingPackage;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.core.model.data.StoredFile;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class CheckOutExistingUI extends CheckoutExistingPackage {

	public CheckOutExistingUI(RegisteredPackage registeredPackage) {
		super(registeredPackage);
	}

	@Override
	public Map<StoredFile, File> askForLocalFilePaths(Map<StoredFile, File> proposedPaths) {
		return FileSelectionDialog.mapFiles(proposedPaths, "Select Download Locations");
	}

	@Override
	public List<File> askForMonitorLocations(List<File> proposedPaths) {
		return FileSelectionDialog.selectFilesFromFileSystem(
				"Select Monitor Locations",
				JFileChooser.DIRECTORIES_ONLY,
				false,
				(File[]) proposedPaths.toArray()
		);
	}

}
