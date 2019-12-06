package org.ajar.bifrost.client.ui.workflow;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.ajar.bifrost.client.comm.PersistenceClient;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.client.workflow.CheckinNewPackage;
import org.ajar.bifrost.core.model.data.LocalFile;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class CheckInNewUI extends CheckinNewPackage {
	
	private final static Pattern stripper = Pattern.compile("(.+//|/).+");
	
	public CheckInNewUI(BifrostPersistenceWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public Map<LocalFile, String> askForRemoteMappings(Map<LocalFile, String> suggestions) {
		return FileSelectionDialog.mapLocations(suggestions, "Select Upload Locations");
	}

	@Override
	public String askForRemoteLocation() {
		PersistenceClient client = MappingSelectionDialog.selectPersistenceMethod("Select a protocol for remote storage.");
		
		if(client != null) {
			String location = JOptionPane.showInputDialog("Please Provide a remote location");
			
			Matcher matcher = stripper.matcher(location);
			
			if(matcher.find()) {
				String prefix = matcher.group(1);
				location = location.replace(prefix, "");
			}
			
			if(!location.startsWith("/")) location = "/" + location;
			return client.prefixLocation(location);
		} else {
			return null;
		}
	}

}
