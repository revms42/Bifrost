package org.ajar.bifrost.client.ui.workflow;

import java.util.List;
import java.util.Map;

import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.client.workflow.CheckinExistingPackage;
import org.ajar.bifrost.core.model.data.LocalFile;

public class CheckInExistingUI extends CheckinExistingPackage {

	public CheckInExistingUI(BifrostPersistenceWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public List<LocalFile> askForNewFiles(List<LocalFile> files) {
		return FileSelectionDialog.selectFilesFromList(files, "Update Files");
	}

	@Override
	public Map<LocalFile, String> askForRemoteMappings(Map<LocalFile, String> suggestions) {
		return FileSelectionDialog.mapLocations(suggestions, "Select New Files");
	}

	@Override
	public List<LocalFile> askForFileDeletions(List<LocalFile> deletions) {
		return FileSelectionDialog.selectFilesFromList(deletions, "Delete Files");
	}

}
