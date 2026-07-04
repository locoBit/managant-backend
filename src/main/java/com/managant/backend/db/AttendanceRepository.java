package com.managant.backend.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
  List<AttendanceEntity> findByEventIdOrderByAttendedAtAsc(Long eventId);
  boolean existsByEventIdAndPersonId(Long eventId, Long personId);

  AttendanceEntity findByEventIdAndPersonId(Long eventId, Long personId);
}
