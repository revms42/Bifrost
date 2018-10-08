package org.ajar.bifrost.monitoring;

import org.ajar.bifrost.tracking.CodexDifference;

public interface Monitor {
	
	public void startMonitor(CodexFilter filter);
	public CodexDifference stopMonitor();
}
