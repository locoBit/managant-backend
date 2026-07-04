package com.managant.backend.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaCategoryRepository extends JpaRepository<AreaCategoryEntity, Long> {
  boolean existsByNameIgnoreCase(String name);
}
