package com.managant.backend.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<AreaEntity, Long> {
  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
  boolean existsByResponsiblePersonIdAndActiveTrue(Long personId);
  boolean existsByHelperPersonIdAndActiveTrue(Long personId);
}
