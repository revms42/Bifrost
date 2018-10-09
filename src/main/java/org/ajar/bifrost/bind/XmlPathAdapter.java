package org.ajar.bifrost.bind;

import java.io.File;
import java.nio.file.Path;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlPathAdapter extends XmlAdapter<String, Path> {

	@Override
	public Path unmarshal(String v) throws Exception {
		return (new File(v)).toPath();
	}

	@Override
	public String marshal(Path v) throws Exception {
		return v == null ? null : v.toString();
	}

}
