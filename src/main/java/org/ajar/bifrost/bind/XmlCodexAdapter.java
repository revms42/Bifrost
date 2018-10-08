package org.ajar.bifrost.bind;

import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.ajar.bifrost.tracking.Codex;
import org.ajar.bifrost.tracking.Leaf;

public abstract class XmlCodexAdapter<L extends Leaf, C extends Codex<L>> extends XmlTrackableAdapater<XmlCodexAdapter.XmlCodexRepresentation, C>{

	@XmlRootElement(name = "codex")
	public static class XmlCodexRepresentation extends XmlTrackableRepresentation implements Codex<XmlLeafAdapter.XmlLeafRepresentation> {
		private Set<XmlLeafAdapter.XmlLeafRepresentation> leaves;
		private String title;
		
		@XmlElementWrapper(name = "leaves")
		@XmlElement(name = "leaf")
		@Override
		public Set<XmlLeafAdapter.XmlLeafRepresentation> getLeaves() {
			return leaves;
		}
		
		@Override
		public void setLeaves(Set<XmlLeafAdapter.XmlLeafRepresentation> leaves) {
			this.leaves = leaves;
		}

		@XmlElement
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}
	}
	
	protected abstract XmlLeafAdapter<L> getLeafAdapter();
	
	@Override
	protected XmlCodexRepresentation createRepresentation() {
		return new XmlCodexRepresentation();
	}

	@Override
	public C unmarshal(XmlCodexRepresentation v) throws Exception {
		C codex = super.unmarshal(v);
		codex.setLeaves(v.getLeaves().stream().map(leaf -> getLeafAdapter().createBound()).collect(Collectors.toSet()));
		codex.setTitle(v.getTitle());
		return codex;
	}

	@Override
	public XmlCodexRepresentation marshal(C v) throws Exception {
		XmlCodexRepresentation xml = super.marshal(v);
		xml.setLeaves(v.getLeaves().stream().map(leaf -> getLeafAdapter().createRepresentation()).collect(Collectors.toSet()));
		xml.setTitle(v.getTitle());
		return xml;
	}
}
