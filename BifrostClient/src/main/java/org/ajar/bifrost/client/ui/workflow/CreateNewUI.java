package org.ajar.bifrost.client.ui.workflow;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.ajar.bifrost.client.ui.HeimdallBifrostClient;
import org.ajar.bifrost.client.ui.JsonFileFilter;
import org.ajar.bifrost.client.workflow.AbstractCreateWorkflow;
import org.ajar.bifrost.core.model.data.LocalFile;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class CreateNewUI extends AbstractCreateWorkflow {

	private final static JFileChooser chooser = HeimdallBifrostClient.chooser;
	
	@Override
	public File askForLocalSaveLocation() {
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setFileFilter(new JsonFileFilter("Heimdall Mapping Files (.json)"));
		
		if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			return null;	
		}
	}
	
	@Override
	public String askForName() {
		return JOptionPane.showInputDialog("Create Mapping Name", "<name>");
	}

	@Override
	public List<LocalFile> askForNewFiles(List<LocalFile> files) {
		return FileSelectionDialog.selectFilesFromList(files, "Select Files To Map");
	}

	@Override
	public List<File> askForMonitorLocations() {
		return FileSelectionDialog.selectFilesFromFileSystem("Add Monitor Locations", JFileChooser.DIRECTORIES_ONLY, false);
	}

	@Override
	public boolean notifyDuplicateName() {
		int ret = JOptionPane.showConfirmDialog(null, "That name is already taken", "Duplicate", JOptionPane.OK_CANCEL_OPTION);
		
		if(ret == JOptionPane.CANCEL_OPTION) {
			return false;
		} else {
			return true;
		}
	}

}
