package org.ajar.bifrost.tracking;

import java.nio.file.Path;

public interface Leaf extends Trackable {

	Path getRelativePath();

	void setRelativePath(Path p);

}