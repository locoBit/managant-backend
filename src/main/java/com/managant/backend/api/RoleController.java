package com.managant.backend.api;

import com.managant.backend.api.dto.RoleDto;
import com.managant.backend.api.dto.RoleUpsertRequest;
import com.managant.backend.db.AreaAllowedRoleEntity;
import com.managant.backend.db.AreaAllowedRoleRepository;
import com.managant.backend.db.AreaRepository;
import com.managant.backend.db.RoleEntity;
import com.managant.backend.db.RoleRepository;
import com.managant.backend.domain.DomainRules;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  private final RoleRepository roleRepo;
  private final AreaRepository areaRepo;
  private final AreaAllowedRoleRepository allowedRoleRepo;
  private final DomainRules rules;

  public RoleController(
      RoleRepository roleRepo,
      AreaRepository areaRepo,
      AreaAllowedRoleRepository allowedRoleRepo,
      DomainRules rules
  ) {
    this.roleRepo = roleRepo;
    this.areaRepo = areaRepo;
    this.allowedRoleRepo = allowedRoleRepo;
    this.rules = rules;
  }

  @GetMapping
  public List<RoleDto> list(jakarta.servlet.http.HttpServletRequest request) {
    if (request.getAttribute(com.managant.backend.auth.AuthTokenFilter.ATTR_CURRENT_USER) == null) {
      throw new SecurityException("No autenticado");
    }
    return roleRepo.findAll().stream()
        .sorted(Comparator.comparing(RoleEntity::getName))
        .map(RoleController::toDto)
        .toList();
  }

  @PostMapping
  public RoleDto create(@Valid @RequestBody RoleUpsertRequest req) {
    var name = req.name().trim();
    var scope = normalizeScope(req.scope());

    rules.ensureUniqueRoleNameAndScope(name, scope, null);

    var role = new RoleEntity();
    role.setName(name);
    role.setScope(scope);
    role.setMaxPeople(req.maxPeople());
    role.setActive(req.active() == null || req.active());

    role = roleRepo.save(role);

    // UX rule: root areas should allow all roles by default.
    // Otherwise, you create a role and can't assign it anywhere (super confusing).
    var rootAreas = areaRepo.findAll().stream()
        .filter(a -> a.isActive() && a.getParentAreaId() == null)
        .toList();

    for (var a : rootAreas) {
      var rel = new AreaAllowedRoleEntity();
      rel.setAreaId(a.getId());
      rel.setRoleId(role.getId());
      allowedRoleRepo.save(rel);
    }

    return toDto(role);
  }

  @PutMapping("/{id}")
  public RoleDto update(@PathVariable Long id, @Valid @RequestBody RoleUpsertRequest req) {
    var role = roleRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));

    var name = req.name().trim();
    var scope = normalizeScope(req.scope());

    rules.ensureUniqueRoleNameAndScope(name, scope, id);

    role.setName(name);
    role.setScope(scope);
    role.setMaxPeople(req.maxPeople());
    if (req.active() != null) role.setActive(req.active());
    return toDto(roleRepo.save(role));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!roleRepo.existsById(id)) {
      throw new IllegalArgumentException("Rol no encontrado");
    }

    rules.ensureRoleDeletable(id);
    roleRepo.deleteById(id);
  }

  private static String normalizeScope(String scope) {
    if (scope == null) throw new IllegalArgumentException("El scope es obligatorio");
    var s = scope.trim().toUpperCase();
    if (!"GLOBAL".equals(s) && !"AREA".equals(s)) {
      throw new IllegalArgumentException("Scope inválido (usa GLOBAL o AREA)");
    }
    return s;
  }

  private static RoleDto toDto(RoleEntity r) {
    return new RoleDto(
        String.valueOf(r.getId()),
        r.getName(),
        r.getScope(),
        r.getMaxPeople(),
        r.isActive()
    );
  }
}
