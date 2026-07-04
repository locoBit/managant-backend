package com.managant.backend.api;

import com.managant.backend.api.dto.UserDto;
import com.managant.backend.api.dto.UserUpsertRequest;
import java.util.regex.Pattern;
import com.managant.backend.db.PersonRepository;
import com.managant.backend.db.UserAccountEntity;
import com.managant.backend.db.UserAccountRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserAccountRepository userRepo;
  private final PersonRepository personRepo;

  public UserController(UserAccountRepository userRepo, PersonRepository personRepo) {
    this.userRepo = userRepo;
    this.personRepo = personRepo;
  }

  @GetMapping
  public List<UserDto> list(jakarta.servlet.http.HttpServletRequest request) {
    if (request.getAttribute(com.managant.backend.auth.AuthTokenFilter.ATTR_CURRENT_USER) == null) {
      throw new SecurityException("No autenticado");
    }
    return userRepo.findAll().stream()
        .sorted(Comparator.comparing(UserAccountEntity::getUsername))
        .map(UserController::toDto)
        .toList();
  }

  private static final Pattern GMAIL_RE = Pattern.compile("^[^@\\s]+@gmail\\.com$", Pattern.CASE_INSENSITIVE);

  // Matches fakeApi.upsertUser
  @PostMapping
  @Transactional
  public UserDto upsert(@Valid @RequestBody UserUpsertRequest req) {
    Long personId = parseLong(req.personId(), "Persona requerida");
    var person = personRepo.findById(personId).orElseThrow(() -> new IllegalArgumentException("Persona requerida"));
    if (!person.isActive()) {
      throw new IllegalArgumentException("Persona requerida");
    }

    String gmail = req.gmail().trim().toLowerCase();
    if (!GMAIL_RE.matcher(gmail).matches()) {
      throw new IllegalArgumentException("Correo inválido (usa @gmail.com)");
    }

    if (userRepo.existsByUsernameAndPersonIdNot(gmail, personId)) {
      throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
    }

    var user = userRepo.findByPersonId(personId).orElseGet(UserAccountEntity::new);
    user.setPersonId(personId);
    user.setUsername(gmail);

    if (req.active() != null) {
      user.setActive(req.active());
    } else if (user.getId() == null) {
      user.setActive(true);
    }

    // Temporary until Google OAuth: use gmail as password so login still works.
    user.setPassword(gmail);

    user = userRepo.save(user);
    return toDto(user);
  }

  private static UserDto toDto(UserAccountEntity u) {
    return new UserDto(
        String.valueOf(u.getId()),
        String.valueOf(u.getPersonId()),
        u.getUsername(),
        u.getPassword(),
        u.isActive()
    );
  }

  private static Long parseLong(String value, String errorMsg) {
    try {
      return Long.parseLong(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException(errorMsg);
    }
  }
}
