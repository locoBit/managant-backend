package com.managant.backend.domain;

import com.managant.backend.db.AreaAllowedRoleRepository;
import com.managant.backend.db.AreaEntity;
import com.managant.backend.db.AreaRepository;
import com.managant.backend.db.PersonEntity;
import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.db.PersonRepository;
import com.managant.backend.db.RoleEntity;
import com.managant.backend.db.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DomainRules {

  private final RoleRepository roleRepo;
  private final PersonRepository personRepo;
  private final AreaRepository areaRepo;
  private final PersonMembershipRepository membershipRepo;
  private final AreaAllowedRoleRepository allowedRoleRepo;

  public DomainRules(
      RoleRepository roleRepo,
      PersonRepository personRepo,
      AreaRepository areaRepo,
      PersonMembershipRepository membershipRepo,
      AreaAllowedRoleRepository allowedRoleRepo
  ) {
    this.roleRepo = roleRepo;
    this.personRepo = personRepo;
    this.areaRepo = areaRepo;
    this.membershipRepo = membershipRepo;
    this.allowedRoleRepo = allowedRoleRepo;
  }

  public RoleEntity requireActiveRole(Long roleId, String msg) {
    var role = roleRepo.findById(roleId).orElseThrow(() -> new IllegalArgumentException(msg));
    if (!role.isActive()) throw new IllegalArgumentException(msg);
    return role;
  }

  public PersonEntity requireActivePerson(Long personId, String msg) {
    var p = personRepo.findById(personId).orElseThrow(() -> new IllegalArgumentException(msg));
    if (!p.isActive()) throw new IllegalArgumentException(msg);
    return p;
  }

  public AreaEntity requireActiveArea(Long areaId, String msg) {
    var a = areaRepo.findById(areaId).orElseThrow(() -> new IllegalArgumentException(msg));
    if (!a.isActive()) throw new IllegalArgumentException(msg);
    return a;
  }

  public void ensureUniqueRoleNameAndScope(String name, String scope, Long roleId) {
    var safeId = roleId == null ? -1L : roleId;
    if (roleRepo.existsByNameIgnoreCaseAndScopeAndIdNot(name, scope, safeId)) {
      throw new IllegalArgumentException("Ya existe un rol con ese nombre y alcance.");
    }
  }

  public void ensureRoleCapacity(RoleEntity role, Long areaIdIfAny) {
    // Source of truth: memberships.
    if (role.getMaxPeople() <= 0) return;

    if ("AREA".equalsIgnoreCase(role.getScope())) {
      if (areaIdIfAny == null) {
        throw new IllegalArgumentException("Área requerida para validar capacidad de un rol de área.");
      }
      long count = membershipRepo.countDistinctActivePeopleByRoleIdAndAreaId(role.getId(), areaIdIfAny);
      if (count >= role.getMaxPeople()) {
        throw new IllegalArgumentException("Se alcanzó el número máximo de personas para este rol.");
      }
    } else {
      long count = membershipRepo.countDistinctActivePeopleByRoleId(role.getId());
      if (count >= role.getMaxPeople()) {
        throw new IllegalArgumentException("Se alcanzó el número máximo de personas para este rol.");
      }
    }
  }

  public void ensureRoleDeletable(Long roleId) {
    if (membershipRepo.existsByRoleId(roleId) || allowedRoleRepo.existsByRoleId(roleId)) {
      throw new IllegalArgumentException("No se puede eliminar un rol asignado a personas.");
    }
  }

  public void ensureRoleAllowedInArea(Long areaId, Long roleId) {
    boolean allowed = allowedRoleRepo.findByAreaId(areaId).stream().anyMatch(x -> x.getRoleId().equals(roleId));
    if (!allowed) {
      throw new IllegalArgumentException("El rol no está permitido en esta área");
    }
  }

  public Set<Long> getDescendantAreaIdsIncludingSelf(Long rootAreaId) {
    // Simple iterative closure over adjacency list.
    var all = areaRepo.findAll();
    Set<Long> ids = new HashSet<>();
    ids.add(rootAreaId);

    boolean changed = true;
    while (changed) {
      changed = false;
      for (var a : all) {
        if (!a.isActive()) continue;
        if (a.getParentAreaId() != null && ids.contains(a.getParentAreaId()) && !ids.contains(a.getId())) {
          ids.add(a.getId());
          changed = true;
        }
      }
    }

    return ids;
  }

  public boolean personIsMemberOfAnyArea(Long personId, Set<Long> allowedAreaIds) {
    var person = personRepo.findById(personId).orElse(null);
    if (person == null || !person.isActive()) return false;

    return membershipRepo.findByPersonId(personId).stream()
        .anyMatch(m -> allowedAreaIds.contains(m.getAreaId()));
  }

  public void ensureAllowedRolesNotEmpty(List<String> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) {
      throw new IllegalArgumentException("Debes seleccionar al menos un rol permitido para el área.");
    }
  }
}
