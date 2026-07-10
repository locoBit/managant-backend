package com.managant.backend.api;

import com.managant.backend.api.dto.ViewerDto;
import com.managant.backend.auth.AuthTokenFilter;
import com.managant.backend.auth.CurrentUser;
import com.managant.backend.db.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

  private final PersonRepository personRepo;

  public MeController(PersonRepository personRepo) {
    this.personRepo = personRepo;
  }

  @GetMapping
  public ViewerDto me(HttpServletRequest request) {
    var viewer = (CurrentUser) request.getAttribute(AuthTokenFilter.ATTR_CURRENT_USER);
    if (viewer == null) {
      throw new SecurityException("No autenticado");
    }

    var person = personRepo.findById(viewer.personId()).orElse(null);
    String name = person == null ? viewer.username() : (person.getFirstNames() + " " + person.getLastName());

    return new ViewerDto(
        String.valueOf(viewer.userId()),
        viewer.username(),
        name,
        String.valueOf(viewer.personId()),
        viewer.isAdmin()
    );
  }
}
