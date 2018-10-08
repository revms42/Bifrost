package org.ajar.bifrost.monitoring;

import java.nio.file.Path;

public interface CodexFilter {

	public boolean matches(Path p);
}
