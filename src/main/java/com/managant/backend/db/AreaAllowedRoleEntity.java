package com.managant.backend.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "area_allowed_role")
@IdClass(AreaAllowedRoleEntity.Pk.class)
public class AreaAllowedRoleEntity {

  @Id
  @Column(name = "area_id", nullable = false)
  private Long areaId;

  @Id
  @Column(name = "role_id", nullable = false)
  private Long roleId;

  public Long getAreaId() {
    return areaId;
  }

  public void setAreaId(Long areaId) {
    this.areaId = areaId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public static class Pk implements Serializable {
    private Long areaId;
    private Long roleId;

    public Pk() {}

    public Pk(Long areaId, Long roleId) {
      this.areaId = areaId;
      this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Pk pk = (Pk) o;
      return Objects.equals(areaId, pk.areaId) && Objects.equals(roleId, pk.roleId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(areaId, roleId);
    }
  }
}
