package com.managant.backend.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
  List<EventEntity> findAllByOrderByStartDateDesc();
}
