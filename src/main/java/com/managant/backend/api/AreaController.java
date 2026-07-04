package com.managant.backend.api;

import com.managant.backend.api.dto.AreaDto;
import com.managant.backend.api.dto.AreaUpsertRequest;
import com.managant.backend.db.AreaAllowedRoleEntity;
import com.managant.backend.db.AreaAllowedRoleRepository;
import com.managant.backend.db.AreaEntity;
import com.managant.backend.db.AreaRepository;
import com.managant.backend.db.PersonRepository;
import com.managant.backend.db.RoleRepository;
import com.managant.backend.domain.DomainRules;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

  private final AreaRepository areaRepo;
  private final AreaAllowedRoleRepository allowedRoleRepo;
  private final PersonRepository personRepo;
  private final RoleRepository roleRepo;
  private final DomainRules rules;

  public AreaController(
      AreaRepository areaRepo,
      AreaAllowedRoleRepository allowedRoleRepo,
      PersonRepository personRepo,
      RoleRepository roleRepo,
      DomainRules rules
  ) {
    this.areaRepo = areaRepo;
    this.allowedRoleRepo = allowedRoleRepo;
    this.personRepo = personRepo;
    this.roleRepo = roleRepo;
    this.rules = rules;
  }

  @GetMapping
  public List<AreaDto> list(jakarta.servlet.http.HttpServletRequest request) {
    // Auth required (handled by filter)
    if (request.getAttribute(com.managant.backend.auth.AuthTokenFilter.ATTR_CURRENT_USER) == null) {
      throw new SecurityException("No autenticado");
    }
    return areaRepo.findAll().stream()
        .sorted(Comparator.comparing(AreaEntity::getName))
        .map(this::toDto)
        .toList();
  }

  @PostMapping
  @Transactional
  public AreaDto create(@Valid @RequestBody AreaUpsertRequest req) {
    if (areaRepo.existsByNameIgnoreCaseAndIdNot(req.name().trim(), -1L)) {
      throw new IllegalArgumentException("Ya existe un área con ese nombre.");
    }

    Long responsibleId = parseLong(req.responsiblePersonId(), "Responsable no válido.");
    rules.requireActivePerson(responsibleId, "Responsable no válido.");

    Long helperId = null;
    if (req.helperPersonId() != null && !req.helperPersonId().isBlank()) {
      helperId = parseLong(req.helperPersonId(), "Ayudante no válido.");
      if (helperId.equals(responsibleId)) {
        throw new IllegalArgumentException("El ayudante no puede ser la misma persona que el responsable.");
      }
      rules.requireActivePerson(helperId, "Ayudante no válido.");
    }

    var area = new AreaEntity();
    area.setName(req.name().trim());
    area.setCategoryId(parseLongOrNull(req.categoryId()));
    area.setParentAreaId(parseLongOrNull(req.parentAreaId()));
    area.setResponsiblePersonId(responsibleId);
    area.setHelperPersonId(helperId);
    area.setActive(req.active() == null || req.active());

    area = areaRepo.save(area);

    syncAllowedRoles(area.getId(), req.allowedRoleIds());

    return toDto(area);
  }

  @PutMapping("/{id}")
  @Transactional
  public AreaDto update(@PathVariable Long id, @Valid @RequestBody AreaUpsertRequest req) {
    var area = areaRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Área no encontrada"));

    if (areaRepo.existsByNameIgnoreCaseAndIdNot(req.name().trim(), id)) {
      throw new IllegalArgumentException("Ya existe un área con ese nombre.");
    }

    Long responsibleId = parseLong(req.responsiblePersonId(), "Responsable no válido.");
    rules.requireActivePerson(responsibleId, "Responsable no válido.");

    Long helperId = null;
    if (req.helperPersonId() != null && !req.helperPersonId().isBlank()) {
      helperId = parseLong(req.helperPersonId(), "Ayudante no válido.");
      if (helperId.equals(responsibleId)) {
        throw new IllegalArgumentException("El ayudante no puede ser la misma persona que el responsable.");
      }
      rules.requireActivePerson(helperId, "Ayudante no válido.");
    }

    area.setName(req.name().trim());
    area.setCategoryId(parseLongOrNull(req.categoryId()));
    area.setParentAreaId(parseLongOrNull(req.parentAreaId()));
    area.setResponsiblePersonId(responsibleId);
    area.setHelperPersonId(helperId);
    if (req.active() != null) area.setActive(req.active());

    area = areaRepo.save(area);

    syncAllowedRoles(area.getId(), req.allowedRoleIds());

    return toDto(area);
  }

  private void syncAllowedRoles(Long areaId, List<String> roleIds) {
    rules.ensureAllowedRolesNotEmpty(roleIds);

    // validate roles exist and active
    for (String roleIdStr : roleIds) {
      Long roleId = parseLong(roleIdStr, "Rol inválido");
      var role = roleRepo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Rol inválido"));
      if (!role.isActive()) throw new IllegalArgumentException("Rol inválido");
    }

    allowedRoleRepo.deleteByAreaId(areaId);
    for (String roleIdStr : roleIds) {
      Long roleId = parseLong(roleIdStr, "Rol inválido");
      var e = new AreaAllowedRoleEntity();
      e.setAreaId(areaId);
      e.setRoleId(roleId);
      allowedRoleRepo.save(e);
    }
  }

  private AreaDto toDto(AreaEntity area) {
    var allowed = allowedRoleRepo.findByAreaId(area.getId()).stream()
        .map(r -> String.valueOf(r.getRoleId()))
        .toList();

    var responsible = personRepo.findById(area.getResponsiblePersonId()).orElse(null);
    String responsibleName = responsible == null
        ? String.valueOf(area.getResponsiblePersonId())
        : (responsible.getFirstNames() + " " + responsible.getLastName()).trim();

    String helperName = null;
    if (area.getHelperPersonId() != null) {
      var helper = personRepo.findById(area.getHelperPersonId()).orElse(null);
      helperName = helper == null
          ? String.valueOf(area.getHelperPersonId())
          : (helper.getFirstNames() + " " + helper.getLastName()).trim();
    }

    return new AreaDto(
        String.valueOf(area.getId()),
        area.getName(),
        area.getCategoryId() == null ? null : String.valueOf(area.getCategoryId()),
        area.getParentAreaId() == null ? null : String.valueOf(area.getParentAreaId()),
        String.valueOf(area.getResponsiblePersonId()),
        responsibleName,
        area.getHelperPersonId() == null ? null : String.valueOf(area.getHelperPersonId()),
        helperName,
        allowed,
        area.isActive()
    );
  }

  private static Long parseLong(String value, String errorMsg) {
    try {
      return Long.parseLong(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  private static Long parseLongOrNull(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Long.parseLong(value);
    } catch (Exception ex) {
      return null;
    }
  }
}
