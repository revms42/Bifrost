package org.ajar.bifrost.client.ui;

import java.io.IOException;
import java.util.HashSet;

import javax.naming.OperationNotSupportedException;

import org.ajar.bifrost.client.Heimdall;
import org.ajar.bifrost.client.model.BifrostPersistenceWrapper;
import org.ajar.bifrost.core.model.data.PackageStatusDescription;
import org.ajar.bifrost.core.model.data.RegisteredPackage;

public class MappingElement implements Comparable<MappingElement> {
	
	public interface MappingDataChangeListener {
		public void dataChanged(MappingElement element);
	}
	
	public enum Status {
		NOT_UPDATED,
		LOCAL_ONLY,
		REMOTE_ONLY,
		LOCAL_BEHIND_REMOTE,
		LOCAL_AHEAD_REMOTE,
		LOCAL_REMOTE_SYNC,
		CHECKED_OUT_LOCAL,
		CHECKED_OUT_REMOTE,
		ERROR;
	}
	
	private PackageStatusDescription packageDescription;
	private BifrostPersistenceWrapper mapping;
	private Status status;
	private final HashSet<MappingDataChangeListener> listeners;
	
	public MappingElement() {
		this.status = Status.NOT_UPDATED;
		this.listeners = new HashSet<>();
	}
	
	public String getName() {
		if(packageDescription != null) {
			return packageDescription.getName();
		} else {
			return mapping.getName();
		}
	}
	
	public PackageStatusDescription getPackageStatusDescription() {
		return packageDescription;
	}
	
	public void setPackageStatusDescription(PackageStatusDescription packageDescription) {
		this.packageDescription = packageDescription;
	}
	
	public BifrostPersistenceWrapper getMapping() {
		return mapping;
	}
	
	public void setMapping(BifrostPersistenceWrapper wrapper) {
		this.mapping = wrapper;
	}

	public Status getStatus() {
		return status;
	}
	
	public void determineStatus() {
		if(packageDescription != null) {
			if(packageDescription.isActive()) {
				if(mapping != null && Heimdall.isMonitoring(mapping)) {
					this.status = Status.CHECKED_OUT_LOCAL;
				} else {
					this.status = Status.CHECKED_OUT_REMOTE;
				}
			} else {
				if(mapping == null) {
					this.status = Status.REMOTE_ONLY;
				} else {
					try {
						long local = mapping.getLocalMapping().getVersion();
						long remote = mapping.getStoredMapping().getVersion();
						if(local > remote) {
							this.status = Status.LOCAL_AHEAD_REMOTE;
						} else if(remote > local) {
							this.status = Status.LOCAL_BEHIND_REMOTE;
						} else {
							this.status = Status.LOCAL_REMOTE_SYNC;
						}
					} catch (OperationNotSupportedException e) {
						e.printStackTrace();
						this.status = Status.ERROR;
					} catch (IOException e) {
						e.printStackTrace();
						this.status = Status.ERROR;
					}
				}
			}
		} else {
			this.status = Status.LOCAL_ONLY;
		}
		notifyListeners();
	}
	
	private void notifyListeners() {
		listeners.forEach(listener -> listener.dataChanged(this));
	}
	
	public void addDataChangeListener(MappingDataChangeListener listener) {
		listeners.add(listener);
	}
	
	public void remoteDataChangeListener(MappingDataChangeListener listener) {
		listeners.remove(listener);
	}
	
	public RegisteredPackage getRegisteredPackage() {
		if(RegisteredPackage.class.isAssignableFrom(packageDescription.getClass())) {
			return (RegisteredPackage) packageDescription;
		} else {
			return null;
		}
	}

	@Override
	public int compareTo(MappingElement o) {
		return getName().compareTo(o.getName());
	}
}
