package com.managant.backend.api;

import com.managant.backend.api.dto.AssignMembershipRequest;
import com.managant.backend.api.dto.ChangeMembershipRoleRequest;
import com.managant.backend.api.dto.UnassignMembershipRequest;
import com.managant.backend.api.dto.PersonDto;
import com.managant.backend.api.dto.PersonMembershipDto;
import com.managant.backend.api.dto.PersonUpsertRequest;
import com.managant.backend.api.dto.SuccessDto;
import com.managant.backend.db.AreaRepository;
import com.managant.backend.db.PersonEntity;
import com.managant.backend.db.PersonMembershipEntity;
import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.db.PersonRepository;
import com.managant.backend.db.RoleRepository;
import com.managant.backend.domain.DomainRules;
import com.managant.backend.domain.CurpVariant;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/people")
public class PersonController {

  private final PersonRepository personRepo;
  private final PersonMembershipRepository membershipRepo;
  private final AreaRepository areaRepo;
  private final RoleRepository roleRepo;
  private final DomainRules rules;

  public PersonController(
      PersonRepository personRepo,
      PersonMembershipRepository membershipRepo,
      AreaRepository areaRepo,
      RoleRepository roleRepo,
      DomainRules rules
  ) {
    this.personRepo = personRepo;
    this.membershipRepo = membershipRepo;
    this.areaRepo = areaRepo;
    this.roleRepo = roleRepo;
    this.rules = rules;
  }

  @GetMapping
  public List<PersonDto> list(jakarta.servlet.http.HttpServletRequest request) {
    if (request.getAttribute(com.managant.backend.auth.AuthTokenFilter.ATTR_CURRENT_USER) == null) {
      throw new SecurityException("No autenticado");
    }
    return personRepo.findAll().stream()
        .sorted(Comparator.comparing(PersonEntity::getLastName))
        .map(this::toDto)
        .toList();
  }

  @PostMapping
  @Transactional
  public PersonDto create(@Valid @RequestBody PersonUpsertRequest req) {
    var birthDate = parseDate(req.birthDate());

    var curp = CurpVariant.generate(req.firstNames(), req.lastName(), valueOrEmpty(req.motherLastName()), birthDate);
    if (personRepo.findByCurp(curp).isPresent()) {
      throw new IllegalArgumentException("Ya existe una persona con esos datos (CURP generado).");
    }

    var p = new PersonEntity();
    p.setFirstNames(req.firstNames().trim());
    p.setLastName(req.lastName().trim());
    p.setMotherLastName(valueOrEmpty(req.motherLastName()).trim());
    p.setBirthDate(birthDate);
    p.setCurp(curp);
    p.setActive(true);
    p = personRepo.save(p);

    // Optional: assign an initial role in the root area (ORGANIZACIÓN).
    // Root areas allow all roles by default (see V5 migration).
    if (req.initialRoleId() != null && !req.initialRoleId().trim().isEmpty()) {
      var roleId = parseLong(req.initialRoleId(), "Rol inválido");

      // Validate role + capacity + allowed-in-root.
      var role = rules.requireActiveRole(roleId, "Rol inválido");
      var rootArea = rules.requireActiveArea(1L, "Área raíz no encontrada");
      rules.ensureRoleAllowedInArea(rootArea.getId(), role.getId());
      rules.ensureRoleCapacity(role, "AREA".equalsIgnoreCase(role.getScope()) ? rootArea.getId() : null);

      if (!membershipRepo.existsByPersonIdAndAreaIdAndRoleId(p.getId(), rootArea.getId(), roleId)) {
        var m = new PersonMembershipEntity();
        m.setPersonId(p.getId());
        m.setAreaId(rootArea.getId());
        m.setRoleId(roleId);
        membershipRepo.save(m);
      }
    }

    return toDto(p);
  }

  @PutMapping("/{id}")
  @Transactional
  public PersonDto update(@PathVariable Long id, @Valid @RequestBody PersonUpsertRequest req) {
    var p = personRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));

    var birthDate = parseDate(req.birthDate());
    var curp = CurpVariant.generate(req.firstNames(), req.lastName(), valueOrEmpty(req.motherLastName()), birthDate);
    if (personRepo.existsByCurpAndIdNot(curp, id)) {
      throw new IllegalArgumentException("Ya existe una persona con esos datos (CURP generado).");
    }

    p.setFirstNames(req.firstNames().trim());
    p.setLastName(req.lastName().trim());
    p.setMotherLastName(valueOrEmpty(req.motherLastName()).trim());
    p.setBirthDate(birthDate);
    p.setCurp(curp);

    p = personRepo.save(p);
    return toDto(p);
  }

  // Matches frontend fakeApi.assignPersonToAreaWithRole
  @PostMapping("/assign")
  @Transactional
  public PersonDto assignMembership(@Valid @RequestBody AssignMembershipRequest req) {
    var personId = parseLong(req.personId(), "Datos inválidos");
    var areaId = parseLong(req.areaId(), "Datos inválidos");
    var roleId = parseLong(req.roleId(), "Datos inválidos");

    // Validate entities exist + active
    rules.requireActivePerson(personId, "Datos inválidos");
    var area = rules.requireActiveArea(areaId, "Datos inválidos");
    var role = rules.requireActiveRole(roleId, "Datos inválidos");

    if (membershipRepo.existsByPersonIdAndAreaIdAndRoleId(personId, areaId, roleId)) {
      throw new IllegalArgumentException("La persona ya tiene ese rol en el área.");
    }

    // Validate area allowed roles
    rules.ensureRoleAllowedInArea(area.getId(), role.getId());

    // Role capacity
    rules.ensureRoleCapacity(role, "AREA".equalsIgnoreCase(role.getScope()) ? areaId : null);

    var m = new PersonMembershipEntity();
    m.setPersonId(personId);
    m.setAreaId(areaId);
    m.setRoleId(roleId);
    membershipRepo.save(m);

    var p = personRepo.findById(personId).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
    return toDto(p);
  }

  @PostMapping("/change-role")
  @Transactional
  public PersonDto changeRole(@Valid @RequestBody ChangeMembershipRoleRequest req) {
    var personId = parseLong(req.personId(), "Datos inválidos");
    var areaId = parseLong(req.areaId(), "Datos inválidos");
    var fromRoleId = parseLong(req.fromRoleId(), "Datos inválidos");
    var toRoleId = parseLong(req.toRoleId(), "Datos inválidos");

    if (fromRoleId.equals(toRoleId)) {
      var p = personRepo.findById(personId).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
      return toDto(p);
    }

    rules.requireActivePerson(personId, "Datos inválidos");
    var area = rules.requireActiveArea(areaId, "Datos inválidos");
    rules.requireActiveRole(fromRoleId, "Datos inválidos");
    var toRole = rules.requireActiveRole(toRoleId, "Datos inválidos");

    if (!membershipRepo.existsByPersonIdAndAreaIdAndRoleId(personId, areaId, fromRoleId)) {
      throw new IllegalArgumentException("La persona no tiene ese rol en el área.");
    }

    if (membershipRepo.existsByPersonIdAndAreaIdAndRoleId(personId, areaId, toRoleId)) {
      throw new IllegalArgumentException("La persona ya tiene ese rol en el área.");
    }

    rules.ensureRoleAllowedInArea(area.getId(), toRole.getId());
    rules.ensureRoleCapacity(toRole, "AREA".equalsIgnoreCase(toRole.getScope()) ? areaId : null);

    membershipRepo.deleteByPersonIdAndAreaIdAndRoleId(personId, areaId, fromRoleId);

    var m = new PersonMembershipEntity();
    m.setPersonId(personId);
    m.setAreaId(areaId);
    m.setRoleId(toRoleId);
    membershipRepo.save(m);

    var p = personRepo.findById(personId).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
    return toDto(p);
  }

  @PostMapping("/unassign")
  @Transactional
  public PersonDto unassignMembership(@Valid @RequestBody UnassignMembershipRequest req) {
    var personId = parseLong(req.personId(), "Datos inválidos");
    var areaId = parseLong(req.areaId(), "Datos inválidos");
    var roleId = parseLong(req.roleId(), "Datos inválidos");

    // Validate entities exist + active
    rules.requireActivePerson(personId, "Datos inválidos");
    rules.requireActiveArea(areaId, "Datos inválidos");
    rules.requireActiveRole(roleId, "Datos inválidos");

    if (!membershipRepo.existsByPersonIdAndAreaIdAndRoleId(personId, areaId, roleId)) {
      throw new IllegalArgumentException("La persona no tiene ese rol en el área.");
    }

    membershipRepo.deleteByPersonIdAndAreaIdAndRoleId(personId, areaId, roleId);

    var p = personRepo.findById(personId).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
    return toDto(p);
  }

  @PostMapping("/{id}/deactivate")
  @Transactional
  public SuccessDto deactivate(@PathVariable Long id) {
    var p = personRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));

    // Domain safety: don't allow deactivating a responsible/helper of an active area.
    if (areaRepo.existsByResponsiblePersonIdAndActiveTrue(id) || areaRepo.existsByHelperPersonIdAndActiveTrue(id)) {
      throw new IllegalArgumentException(
          "No puedes desactivar a una persona que es responsable/ayudante de un área activa."
      );
    }

    p.setActive(false);
    personRepo.save(p);
    return SuccessDto.ok();
  }

  private PersonDto toDto(PersonEntity p) {
    var memberships = membershipRepo.findByPersonId(p.getId()).stream()
        .map(m -> new PersonMembershipDto(String.valueOf(m.getAreaId()), String.valueOf(m.getRoleId())))
        .toList();

    return new PersonDto(
        String.valueOf(p.getId()),
        p.getFirstNames(),
        p.getLastName(),
        p.getMotherLastName(),
        p.getBirthDate().toString(),
        p.getCurp(),
        memberships,
        p.isActive()
    );
  }

  private static LocalDate parseDate(String s) {
    try {
      return LocalDate.parse(s);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Fecha de nacimiento inválida");
    }
  }

  private static Long parseLong(String value, String errorMsg) {
    try {
      return Long.parseLong(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  private static String valueOrEmpty(String s) {
    return s == null ? "" : s;
  }
}
