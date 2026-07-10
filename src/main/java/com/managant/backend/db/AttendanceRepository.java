package com.managant.backend.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
  List<AttendanceEntity> findByEventIdOrderByAttendedAtAsc(Long eventId);
  boolean existsByEventIdAndPersonId(Long eventId, Long personId);

  AttendanceEntity findByEventIdAndPersonId(Long eventId, Long personId);

  interface PersonCountRow {
    Long getPersonId();
    Long getCnt();
  }

  interface PersonMaxAttendedAtRow {
    Long getPersonId();
    java.time.LocalDateTime getLastAttendedAt();
  }

  @org.springframework.data.jpa.repository.Query(
      "select a.personId as personId, count(a.id) as cnt "
          + "from AttendanceEntity a "
          + "where a.eventId in :eventIds and a.personId in :personIds "
          + "group by a.personId"
  )
  List<PersonCountRow> countByEventIdsAndPersonIds(
      @org.springframework.data.repository.query.Param("eventIds") java.util.Set<Long> eventIds,
      @org.springframework.data.repository.query.Param("personIds") java.util.Set<Long> personIds
  );

  @org.springframework.data.jpa.repository.Query(
      "select a.personId as personId, max(a.attendedAt) as lastAttendedAt "
          + "from AttendanceEntity a "
          + "where a.eventId in :eventIds and a.personId in :personIds "
          + "group by a.personId"
  )
  List<PersonMaxAttendedAtRow> maxAttendedAtByEventIdsAndPersonIds(
      @org.springframework.data.repository.query.Param("eventIds") java.util.Set<Long> eventIds,
      @org.springframework.data.repository.query.Param("personIds") java.util.Set<Long> personIds
  );
}
