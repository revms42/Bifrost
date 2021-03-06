package org.ajar.bifrost.client.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public class JsonFileFilter extends FileFilter {

	private final String desc;
	
	public JsonFileFilter(String desc) {
		this.desc = desc;
	}
	
	@Override
	public boolean accept(File f) {
		return f.getAbsolutePath().endsWith(".json") || f.isDirectory();
	}

	@Override
	public String getDescription() {
		return desc;
	}
}
