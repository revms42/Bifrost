package org.ajar.bifrost.server.service;

import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.StreamSupport;

import org.ajar.bifrost.core.model.call.RegisterPackage;
import org.ajar.bifrost.core.model.data.RegisteredPackage;
import org.ajar.bifrost.server.model.RegisteredPackageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class PackageService {
	
	@Autowired
	PackageRepository list;

	public RegisteredPackage getPackageByName(String name) {
		return list.findByName(name);
	}
	
	private RegisteredPackageEntity getPackageEntityByName(String name) {
		return list.findByName(name);
	}
	
	public RegisteredPackage updatePackage(RegisterPackage mapping) {
		RegisteredPackageEntity update = getPackageEntityByName(mapping.getName());
		
		if(update == null) {
			update = new RegisteredPackageEntity(mapping.getName(), mapping.getLocation(), 1L, false);
		} else {
			update.setVersion(update.getVersion() + 1L);
			update.setLocation(mapping.getLocation());
			update.setActive(false);
		}
		
		return list.save(update);
	}
	
	public RegisteredPackage activatePackage(RegisterPackage mapping, String remoteAddress) {
		RegisteredPackageEntity update = getPackageEntityByName(mapping.getName());
		
		if(update != null) {
			update.setLocation(remoteAddress + ":" + mapping.getLocation());
			update.setActive(true);
		}
		
		return list.save(update);
	}
	
	public RegisteredPackage setVersion(String name, long version) {
		RegisteredPackageEntity update = getPackageEntityByName(name);
		update.setVersion(version);
		return list.save(update);
	}
	
	public List<RegisteredPackage> getPackageList() {
		return StreamSupport.stream(list.findAll().spliterator(), false).collect(toList());
	}
	
	public List<RegisteredPackage> getPackageSubList(int startIndex, int endIndex) {
		return getPackageList().subList(startIndex, endIndex);
	}
	
	public RegisteredPackage deletePackage(String name) {
		RegisteredPackageEntity delete = getPackageEntityByName(name);
		if(delete != null) {
			list.delete(delete);
		}
		return delete;
	}
}
