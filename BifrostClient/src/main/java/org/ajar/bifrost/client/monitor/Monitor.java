package org.ajar.bifrost.client.monitor;

import java.io.File;
import java.util.Map;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class Monitor {

	public static final String MONITOR_KEY = "monitor.dir";
	
	private File root;
	private Map<String, Long> watchMap;

	public Monitor(File root, Map<String, Long> watchMap) {
		super();
		this.watchMap = watchMap;
		this.root = root;
	}

	public Map<String, Long> getWatchMap() {
		return watchMap;
	}

	public void setWatchMap(Map<String, Long> watchMap) {
		this.watchMap = watchMap;
	}

	public File getRoot() {
		return root;
	}

	public void setRoot(File root) {
		this.root = root;
	}
}
