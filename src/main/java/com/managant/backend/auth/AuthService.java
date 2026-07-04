package com.managant.backend.auth;

import com.managant.backend.auth.dto.LoginResponse;
import com.managant.backend.db.PersonRepository;
import com.managant.backend.db.RoleRepository;
import com.managant.backend.db.UserAccountRepository;
import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.domain.Authz;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserAccountRepository userRepo;
  private final PersonRepository personRepo;
  private final RoleRepository roleRepo;
  private final PersonMembershipRepository membershipRepo;
  private final TokenService tokenService;

  public AuthService(
      UserAccountRepository userRepo,
      PersonRepository personRepo,
      RoleRepository roleRepo,
      PersonMembershipRepository membershipRepo,
      TokenService tokenService
  ) {
    this.userRepo = userRepo;
    this.personRepo = personRepo;
    this.roleRepo = roleRepo;
    this.membershipRepo = membershipRepo;
    this.tokenService = tokenService;
  }

  public LoginResponse loginWithGoogle(String gmail) {
    var user = userRepo.findByUsername(gmail)
        .filter(u -> u.isActive())
        .orElseThrow(() -> new IllegalArgumentException("No autorizado"));

    var person = personRepo.findById(user.getPersonId()).orElse(null);
    if (person != null && !person.isActive()) {
      throw new IllegalArgumentException("No autorizado");
    }

    boolean isAdmin = Authz.personHasAdminRole(user.getPersonId(), roleRepo, membershipRepo);
    String name = person == null ? user.getUsername() : (person.getFirstNames() + " " + person.getLastName());

    // Longer session for Google login.
    String token = tokenService.issueToken(user.getId(), 72);

    return new LoginResponse(
        String.valueOf(user.getId()),
        user.getUsername(),
        name,
        String.valueOf(user.getPersonId()),
        isAdmin,
        token
    );
  }

}
