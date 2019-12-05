package org.ajar.bifrost.client.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class PropertiesFileFilter extends FileFilter {
	
	public static final PropertiesFileFilter singleton = new PropertiesFileFilter();

	@Override
	public boolean accept(File f) {
		return f.getAbsolutePath().endsWith(".properties");
	}

	@Override
	public String getDescription() {
		return "Heimdall Properties Files";
	}

}
