package com.managant.backend.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "person")
public class PersonEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "first_names", nullable = false)
  private String firstNames;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "mother_last_name", nullable = false)
  private String motherLastName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private String curp;

  @Column(nullable = false)
  private boolean active = true;

  public Long getId() {
    return id;
  }

  public String getFirstNames() {
    return firstNames;
  }

  public void setFirstNames(String firstNames) {
    this.firstNames = firstNames;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMotherLastName() {
    return motherLastName;
  }

  public void setMotherLastName(String motherLastName) {
    this.motherLastName = motherLastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getCurp() {
    return curp;
  }

  public void setCurp(String curp) {
    this.curp = curp;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
