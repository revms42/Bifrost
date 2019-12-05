package org.ajar.bifrost.client.workflow;

import java.io.File;
import java.util.List;

import org.ajar.bifrost.core.model.data.LocalFile;

public interface CreateWorkflow extends HeimdallWorkflow{

	public String askForName();
	public boolean notifyDuplicateName();
	public List<LocalFile> askForNewFiles(List<LocalFile> files);
	public List<File> askForMonitorLocations();
	public File askForLocalSaveLocation();
}
