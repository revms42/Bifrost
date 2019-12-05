package org.ajar.bifrost.client.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.client.ui.MappingElement.MappingDataChangeListener;
import org.ajar.bifrost.core.model.call.PackageListResponse;
import org.ajar.bifrost.core.model.call.PackageSummary;

public class MappingListModel implements ListModel<MappingElement>, MappingDataChangeListener {
	
	private final ArrayList<MappingElement> list = new ArrayList<>();
	private final HashSet<ListDataListener> listeners = new HashSet<>();
	
	public void setupList() throws IOException {
		list.clear();
		HashMap<String, MappingElement> map = new HashMap<>();
		
		PackageListResponse response = Heimdall.getClient().getPackageList(0, -1);
		
		if(response != null) {
			response.getRegistered().forEach(summary -> {
				MappingElement element = new MappingElement();
				element.setPackageStatusDescription(summary);
				map.put(element.getName(), element);
			});
		}
		
		Heimdall.getInventory().forEach((name, wrapper) -> {
			if(map.containsKey(name)) {
				map.get(name).setMapping(wrapper);
			} else {
				MappingElement element = new MappingElement();
				element.setMapping(wrapper);
				map.put(name, element);
			}
		});
		
		map.values().forEach(element -> {
			element.determineStatus();
			element.addDataChangeListener(this);
		});
		
		
		list.addAll(map.values());
		Collections.sort(list);
		
		listeners.forEach(listener -> listener.contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, list.size() - 1)));
	}
	
	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public MappingElement getElementAt(int index) {
		return list.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	@Override
	public void dataChanged(MappingElement element) {
		int index = list.indexOf(element);
		listeners.forEach(listener -> listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index)));
	}
	
	public MappingElement find(BifrostPersistenceWrapper wrapper) {
		return list.parallelStream().filter(element -> element.getMapping() == wrapper).findFirst().orElse(null);
	}
	
	public MappingElement find(PackageSummary summary) {
		return list.parallelStream().filter(element -> element.getPackageStatusDescription() == summary).findFirst().orElse(null);
	}
	
	public void addNewElement(BifrostPersistenceWrapper wrapper) {
		MappingElement element = new MappingElement();
		element.setMapping(wrapper);
		list.add(element);
		
		Collections.sort(list);
		
		int index = list.indexOf(element);
		
		listeners.forEach(listener -> {
			listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index+1));	
		});
	}

	public void deleteElement(MappingElement element) {
		int index = list.indexOf(element);
		
		listeners.forEach(listener -> listener.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index)));
	}
}
