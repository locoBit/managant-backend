package com.managant.backend.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
  Optional<RoleEntity> findByName(String name);
  boolean existsByNameIgnoreCaseAndScopeAndIdNot(String name, String scope, Long id);
}
