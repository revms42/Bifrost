package org.ajar.bifrost.tracking;

import java.io.File;
import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlLocalLeafAdapter;

@XmlJavaTypeAdapter(XmlLocalLeafAdapter.class)
@XmlRootElement(name = "leaf")
public class LocalFileLeaf extends AbstractLeaf {
	
	private File file;
	
	public LocalFileLeaf() {}
	
	public LocalFileLeaf(File codex, File file) {
		setRelativePath(codex.toPath().relativize(file.toPath()));
	}
	
	public LocalFileLeaf(Path file) {
		setRelativePath(file);
	}
	
	protected void setLocalFile(File codex) {
		file = codex.toPath().resolve(getRelativePath()).toFile();
	}
	
	@Override
	@XmlAttribute
	public long getVersion() {
		return (file != null && file.exists()) ? file.lastModified() : -1;
	}

	@Override
	public String toString() {
		return file.getPath();
	}

	@Override
	public void setVersion(long version) {
		if(file != null && file.exists()) file.setLastModified(version);
	}
}
