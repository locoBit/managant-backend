package com.managant.backend.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class AttendanceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false)
  private Long eventId;

  @Column(name = "person_id", nullable = false)
  private Long personId;

  @Column(name = "attended_at", nullable = false)
  private LocalDateTime attendedAt;

  @Column(name = "observations", columnDefinition = "TEXT")
  private String observations;

  public Long getId() {
    return id;
  }

  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public Long getPersonId() {
    return personId;
  }

  public void setPersonId(Long personId) {
    this.personId = personId;
  }

  public LocalDateTime getAttendedAt() {
    return attendedAt;
  }

  public void setAttendedAt(LocalDateTime attendedAt) {
    this.attendedAt = attendedAt;
  }

  public String getObservations() {
    return observations;
  }

  public void setObservations(String observations) {
    this.observations = observations;
  }
}
