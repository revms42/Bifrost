package org.ajar.bifrost.monitoring;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

import org.ajar.bifrost.tracking.CodexDifference;
import org.ajar.bifrost.tracking.LocalFileCodex;
import org.ajar.bifrost.tracking.LocalFileLeaf;

import static java.nio.file.StandardWatchEventKinds.*;

public class LocalFileCodexMonitor implements Monitor, Runnable {
	
	private final static WatchService service;
	private WatchKey key;
	
	static {
		service = createWatchService();
	}
	
	private static WatchService createWatchService() {
		try {
			return FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private final Path p;
	private LocalFileCodex.LocalFileCodexDifference difference;
	private CodexFilter filter;
	private Thread localThread;
	
	public LocalFileCodexMonitor(LocalFileCodex codex) {
		this.p = codex.getRoot().toPath();
	}
	
	@Override
	public synchronized void startMonitor(CodexFilter filter) {
		this.difference = new LocalFileCodex.LocalFileCodexDifference();
		this.filter = filter;
		try {
			key = p.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			localThread = new Thread(this);
			localThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized CodexDifference stopMonitor() {
		key.cancel();
		if(localThread.isAlive()) {
			try {
				localThread.join(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return difference;
	}

	@Override
	public void run() {
		if(service != null) {
			try {
				while((key = service.take()) != null) {
					if(key.isValid()) {
						List<WatchEvent<?>> l = key.pollEvents();
						
						handleEvents(l);
						if(!key.reset()) break;
					} else {
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleEvents(List<WatchEvent<?>> l) {
		for(WatchEvent<?> event : l) {
			Kind<?> kind = event.kind();
			
			if(kind.name() == ENTRY_CREATE.name()) {
				handleCreate((WatchEvent<Path>) event);
			} else if(kind.name() == ENTRY_DELETE.name()) {
				handleDelete((WatchEvent<Path>) event);
			} else if(kind.name() == ENTRY_MODIFY.name()) {
				handleChange((WatchEvent<Path>) event);
			} else {
				System.err.println("Cannot handle watch event " + kind.name());
			}
		}
	}
	
	private boolean filterMatches(Path p) {
		return filter != null && filter.matches(p);
	}
	
	private synchronized void handleCreate(WatchEvent<Path> e) {
		if(filterMatches(e.context())) return;
		
		LocalFileLeaf added = new LocalFileLeaf(e.context());
		
		if(difference.removedLeaves().remove(added)) {
			// If you have previously removed this leaf.
			// Then it is modified.
			difference.changedLeaves().add(added);
		}else {
			// If you not have previously removed this leaf.
			// Then it is new, and you should add it.
			difference.addedLeaves().add(added);
		}
	}
	
	private synchronized void handleDelete(WatchEvent<Path> e) {
		if(filterMatches(e.context())) return;
		
		LocalFileLeaf deleted = new LocalFileLeaf(e.context());
		
		if(!difference.addedLeaves().remove(deleted)) {
			// If you not have previously added this leaf.
			// Then it is a removed leaf, scrub anything from changed as well.
			difference.removedLeaves().add(deleted);
		}
		difference.changedLeaves().remove(deleted);
	}
	
	private synchronized void handleChange(WatchEvent<Path> e) {
		if(filterMatches(e.context())) return;
		
		LocalFileLeaf changed = new LocalFileLeaf(e.context());
		
		if(!difference.addedLeaves().contains(changed)) {
			// If you haven't previously added this file
			// Then it is a changed file.
			difference.changedLeaves().add(changed);
		}
	}
}
