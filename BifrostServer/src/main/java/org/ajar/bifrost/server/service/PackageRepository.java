package org.ajar.bifrost.server.service;

import org.ajar.bifrost.server.model.RegisteredPackageEntity;
import org.springframework.data.repository.CrudRepository;

public interface PackageRepository extends CrudRepository<RegisteredPackageEntity, String>{

	RegisteredPackageEntity findByName(String name);
}
