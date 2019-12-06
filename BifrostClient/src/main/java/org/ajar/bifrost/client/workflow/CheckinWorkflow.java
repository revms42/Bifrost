package org.ajar.bifrost.client.workflow;

import java.util.Map;

import org.ajar.bifrost.core.model.data.LocalFile;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public interface CheckinWorkflow extends HeimdallWorkflow {

	public Map<LocalFile, String> askForRemoteMappings(Map<LocalFile, String> suggestions);
}
