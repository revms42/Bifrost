package org.ajar.bifrost.bind;

import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.tracking.Leaf;

public abstract class XmlLeafAdapter<L extends Leaf> extends XmlTrackableAdapater<XmlLeafAdapter.XmlLeafRepresentation, L> {

	@XmlRootElement(name = "leaf")
	public static class XmlLeafRepresentation extends XmlTrackableRepresentation implements Leaf {
		
		private Path path;
		
		@XmlAttribute(name = "path")
		@XmlJavaTypeAdapter(XmlPathAdapter.class)
		@Override
		public Path getRelativePath() {
			return path;
		}

		@Override
		public void setRelativePath(Path p) {
			this.path = p;
		}
	}

	@Override
	protected XmlLeafRepresentation createRepresentation() {
		return new XmlLeafRepresentation();
	}

	@Override
	public L unmarshal(XmlLeafRepresentation v) throws Exception {
		L leaf = super.unmarshal(v);
		leaf.setRelativePath(v.getRelativePath());
		return leaf;
	}

	@Override
	public XmlLeafRepresentation marshal(L v) throws Exception {
		XmlLeafRepresentation xml = super.marshal(v);
		xml.setRelativePath(v.getRelativePath());
		return xml;
	}
}
