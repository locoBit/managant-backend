package com.managant.backend.api;

import com.managant.backend.api.dto.PersonSearchDto;
import com.managant.backend.auth.AuthTokenFilter;
import com.managant.backend.db.PersonEntity;
import com.managant.backend.db.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/people-search")
public class PeopleSearchController {

  private final PersonRepository personRepo;

  public PeopleSearchController(PersonRepository personRepo) {
    this.personRepo = personRepo;
  }

  @GetMapping
  public List<PersonSearchDto> search(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
      HttpServletRequest request
  ) {
    if (request.getAttribute(AuthTokenFilter.ATTR_CURRENT_USER) == null) {
      throw new SecurityException("No autenticado");
    }

    String query = q == null ? "" : q.trim();
    if (query.length() < 2) return List.of();

    int safeLimit = Math.max(1, Math.min(50, limit));

    return personRepo.searchActiveByName(query).stream()
        .sorted(Comparator.comparing(PersonEntity::getLastName).thenComparing(PersonEntity::getFirstNames))
        .limit(safeLimit)
        .map(p -> new PersonSearchDto(
            String.valueOf(p.getId()),
            (p.getFirstNames() + " " + p.getLastName()).trim(),
            p.getFirstNames(),
            p.getLastName(),
            p.getMotherLastName()
        ))
        .toList();
  }
}
