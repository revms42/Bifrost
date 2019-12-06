package org.ajar.bifrost.server.service;

import org.ajar.bifrost.server.model.RegisteredPackageEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * @author revms42
 * @since 0.0.1-SNAPSHOT
 */
public interface PackageRepository extends CrudRepository<RegisteredPackageEntity, String>{

	RegisteredPackageEntity findByName(String name);
}
