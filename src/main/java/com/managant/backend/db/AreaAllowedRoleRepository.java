package com.managant.backend.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaAllowedRoleRepository extends JpaRepository<AreaAllowedRoleEntity, AreaAllowedRoleEntity.Pk> {
  List<AreaAllowedRoleEntity> findByAreaId(Long areaId);
  void deleteByAreaId(Long areaId);
  boolean existsByRoleId(Long roleId);
}
