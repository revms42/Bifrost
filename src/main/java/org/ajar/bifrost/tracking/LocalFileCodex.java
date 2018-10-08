package org.ajar.bifrost.tracking;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ajar.bifrost.bind.XmlLocalFileCodexAdapter;
import org.ajar.bifrost.monitoring.CodexFilter;
import org.ajar.bifrost.monitoring.LocalFileCodexMonitor;
import org.ajar.bifrost.monitoring.Monitor;

@XmlRootElement(name = "codex")
@XmlJavaTypeAdapter(XmlLocalFileCodexAdapter.class)
public class LocalFileCodex extends AbstractCodex<LocalFileLeaf> {

	private File root;
	
	public LocalFileCodex() {
		super();
		setLeaves(new HashSet<>());
	}
	
	public LocalFileCodex(String title, File root, long startVersion) {
		this();
		this.root = root;
		setTitle(title);
		setVersion(startVersion);
	}
	
	@XmlAttribute
	public File getRoot() {
		return root;
	}
	
	public void setRoot(File root) {
		this.root = root;
	}
	
	public void incrementVersion() {
		this.setVersion(this.getVersion() + 1);
	}
	
	@Override
	@XmlElementWrapper(name = "leaves")
	@XmlElement(name = "leaf")
	public Set<LocalFileLeaf> getLeaves() {
		return super.getLeaves();
	}
	
	public void addLeaf(File file) {
		LocalFileLeaf leaf = new LocalFileLeaf(getRoot(), file);
		getLeaves().add(leaf);
	}
	
	public Leaf removeLeaf(File file) {
		Path p = root.toPath().relativize(file.toPath());
		
		Leaf found = getLeaves().stream()
			.filter(leaf -> leaf.getRelativePath().equals(p))
			.findFirst().orElse(null);
		
		if(found != null) getLeaves().remove(found);
		
		return found;
	}
	
	@Override
	public CodexDifference describeDifference(Codex<?> original) {
		CodexDifference cd = new LocalFileCodexDifference();
		
		Set<Leaf> current = new HashSet<>();
		current.addAll(getLeaves());
		current.removeAll(original.getLeaves());
		
		cd.addedLeaves().addAll(current);
		
		current.clear();
		current.addAll(original.getLeaves());
		current.removeAll(getLeaves());
		
		cd.removedLeaves().addAll(current);
		
		current.clear();
		current.addAll(getLeaves());
		current.retainAll(original.getLeaves());
		
		ArrayList<AbstractLeaf> tempList = new ArrayList<>();
		current = current.stream().filter(leaf -> {
			int i = tempList.indexOf(leaf);
			if(i >= 0) {
				return leaf.getVersion() == tempList.get(i).getVersion();
			} else {
				return false;
			}
		}).collect(Collectors.toSet());
		
		cd.changedLeaves().addAll(current);
		
		return cd;
	}
	
	public Monitor monitorCodex(CodexFilter filter) {
		LocalFileCodexMonitor monitor = new LocalFileCodexMonitor(this);
		
		monitor.startMonitor(filter);
		
		return monitor;
	}

	public static class LocalFileCodexDifference implements CodexDifference {

		private final Set<Leaf> changedLeaves = new HashSet<>();
		private final Set<Leaf> addedLeaves = new HashSet<>();
		private final Set<Leaf> removedLeaves = new HashSet<>();
		
		@Override
		public Set<Leaf> changedLeaves() {
			return changedLeaves;
		}

		@Override
		public Set<Leaf> addedLeaves() {
			return addedLeaves;
		}

		@Override
		public Set<Leaf> removedLeaves() {
			return removedLeaves;
		}
		
	}
}
