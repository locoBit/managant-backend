package com.managant.backend.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "area")
public class AreaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "parent_area_id")
  private Long parentAreaId;

  @Column(name = "responsible_person_id", nullable = false)
  private Long responsiblePersonId;

  @Column(name = "helper_person_id")
  private Long helperPersonId;

  @Column(nullable = false)
  private boolean active = true;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public Long getParentAreaId() {
    return parentAreaId;
  }

  public void setParentAreaId(Long parentAreaId) {
    this.parentAreaId = parentAreaId;
  }

  public Long getResponsiblePersonId() {
    return responsiblePersonId;
  }

  public void setResponsiblePersonId(Long responsiblePersonId) {
    this.responsiblePersonId = responsiblePersonId;
  }

  public Long getHelperPersonId() {
    return helperPersonId;
  }

  public void setHelperPersonId(Long helperPersonId) {
    this.helperPersonId = helperPersonId;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
