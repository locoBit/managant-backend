package com.managant.backend.domain;

import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.db.RoleRepository;

public final class Authz {
  private Authz() {}

  public static boolean personHasAdminRole(Long personId, RoleRepository roleRepo, PersonMembershipRepository membershipRepo) {
    var adminRoleOpt = roleRepo.findByName("ADMIN");
    if (adminRoleOpt.isEmpty()) return false;
    var adminRole = adminRoleOpt.get();

    return membershipRepo.findByPersonId(personId).stream()
        .anyMatch(m -> m.getRoleId().equals(adminRole.getId()));
  }
}
