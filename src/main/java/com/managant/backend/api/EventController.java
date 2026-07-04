package com.managant.backend.api;

import com.managant.backend.api.dto.AttendanceDto;
import com.managant.backend.api.dto.EventCreateRequest;
import com.managant.backend.api.dto.EventDto;
import com.managant.backend.api.dto.RegisterAttendanceRequest;
import com.managant.backend.api.dto.UpdateAttendanceRequest;
import com.managant.backend.api.dto.UpdateEventObservationsRequest;
import com.managant.backend.api.dto.UpsertAttendanceRequest;
import com.managant.backend.auth.AuthTokenFilter;
import com.managant.backend.auth.CurrentUser;
import com.managant.backend.db.AreaEntity;
import com.managant.backend.db.AreaRepository;
import com.managant.backend.db.AttendanceEntity;
import com.managant.backend.db.AttendanceRepository;
import com.managant.backend.db.EventEntity;
import com.managant.backend.db.EventRepository;
import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.db.RoleRepository;
import com.managant.backend.domain.Authz;
import com.managant.backend.domain.DomainRules;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

  private final EventRepository eventRepo;
  private final AttendanceRepository attendanceRepo;
  private final AreaRepository areaRepo;
  private final RoleRepository roleRepo;
  private final PersonMembershipRepository membershipRepo;
  private final DomainRules rules;

  public EventController(
      EventRepository eventRepo,
      AttendanceRepository attendanceRepo,
      AreaRepository areaRepo,
      RoleRepository roleRepo,
      PersonMembershipRepository membershipRepo,
      DomainRules rules
  ) {
    this.eventRepo = eventRepo;
    this.attendanceRepo = attendanceRepo;
    this.areaRepo = areaRepo;
    this.roleRepo = roleRepo;
    this.membershipRepo = membershipRepo;
    this.rules = rules;
  }

  @GetMapping
  public List<EventDto> list(HttpServletRequest request) {
    var viewer = requireViewer(request);
    var all = eventRepo.findAllByOrderByStartDateDesc();

    if (viewer.isAdmin()) {
      return all.stream().map(EventController::toDto).toList();
    }

    // Non-admin: only events in managed branches (areas where responsible/helper and descendants)
    var areas = areaRepo.findAll().stream().filter(AreaEntity::isActive).toList();
    var managedRoots = areas.stream()
        .filter(a -> viewer.personId().equals(a.getResponsiblePersonId())
            || (a.getHelperPersonId() != null && viewer.personId().equals(a.getHelperPersonId())))
        .toList();

    java.util.Set<Long> visibleAreaIds = new java.util.HashSet<>();
    for (var root : managedRoots) {
      visibleAreaIds.addAll(rules.getDescendantAreaIdsIncludingSelf(root.getId()));
    }

    return all.stream()
        .filter(e -> visibleAreaIds.contains(e.getAreaId()))
        .map(EventController::toDto)
        .toList();
  }

  @PostMapping
  @Transactional
  public EventDto create(@Valid @RequestBody EventCreateRequest req, HttpServletRequest request) {
    var viewer = requireViewer(request);

    LocalDate start = parseDate(req.startDate(), "Rango de fechas inválido");
    LocalDate end = parseDate(req.endDate(), "Rango de fechas inválido");
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("Rango de fechas inválido");
    }

    Long areaId = parseLong(req.areaId(), "Área no encontrada");
    var area = rules.requireActiveArea(areaId, "Área no encontrada");

    // createdByPersonId is derived from token; we don't trust the client.
    Long createdByPersonId = viewer.personId();

    boolean isAdmin = Authz.personHasAdminRole(createdByPersonId, roleRepo, membershipRepo);

    if (!isAdmin
        && !createdByPersonId.equals(area.getResponsiblePersonId())
        && (area.getHelperPersonId() == null || !createdByPersonId.equals(area.getHelperPersonId()))
    ) {
      throw new IllegalArgumentException(
          "Solo el responsable, ayudante o un ADMIN pueden crear eventos para esta área."
      );
    }

    var e = new EventEntity();
    e.setTitle(req.title().trim());
    e.setAreaId(areaId);
    e.setStartDate(start);
    e.setEndDate(end);
    e.setCreatedByPersonId(createdByPersonId);
    e.setActive(true);

    e = eventRepo.save(e);
    return toDto(e);
  }

  @PostMapping("/attendance")
  @Transactional
  public AttendanceDto registerAttendance(
      @Valid @RequestBody RegisterAttendanceRequest req,
      HttpServletRequest request
  ) {
    // Backwards-compatible endpoint: always registers presence = true.
    requireViewer(request);

    var upsert = new UpsertAttendanceRequest(req.eventId(), req.personId(), true, req.includeSubareas(), null);
    return upsertAttendance(upsert, request);
  }

  @PostMapping("/attendance/upsert")
  @Transactional
  public AttendanceDto upsertAttendance(
      @Valid @RequestBody UpsertAttendanceRequest req,
      HttpServletRequest request
  ) {
    // Auth required, but attendance rule is independent of who registers.
    requireViewer(request);

    Long eventId = parseLong(req.eventId(), "Evento no encontrado");
    Long personId = parseLong(req.personId(), "Persona no encontrada");
    boolean present = req.present() == null || req.present();
    boolean includeSubareas = req.includeSubareas() != null && req.includeSubareas();

    var event = eventRepo.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    if (!event.isActive()) throw new IllegalArgumentException("Evento no encontrado");

    rules.requireActivePerson(personId, "Persona no encontrada");

    // Backend enforcement: by default only exact area. If includeSubareas=true allow descendants.
    var allowedAreaIds = includeSubareas
        ? rules.getDescendantAreaIdsIncludingSelf(event.getAreaId())
        : java.util.Set.of(event.getAreaId());

    if (!rules.personIsMemberOfAnyArea(personId, allowedAreaIds)) {
      throw new IllegalArgumentException(
          "La persona no pertenece al área (o rama) del evento, no puede asistir."
      );
    }

    var existing = attendanceRepo.findByEventIdAndPersonId(eventId, personId);

    if (!present) {
      // Uncheck = delete attendance if exists.
      if (existing != null) {
        attendanceRepo.deleteById(existing.getId());
      }
      return new AttendanceDto("", String.valueOf(eventId), String.valueOf(personId), "", null);
    }

    // present=true
    if (existing != null) {
      existing.setObservations(req.observations() == null ? null : req.observations().trim());
      existing = attendanceRepo.save(existing);
      return toDto(existing);
    }

    var a = new AttendanceEntity();
    a.setEventId(eventId);
    a.setPersonId(personId);
    a.setAttendedAt(LocalDateTime.now());
    a.setObservations(req.observations() == null ? null : req.observations().trim());

    a = attendanceRepo.save(a);
    return toDto(a);
  }

  @GetMapping("/{eventId}/attendance")
  public List<AttendanceDto> listAttendance(@PathVariable Long eventId, HttpServletRequest request) {
    requireViewer(request);

    return attendanceRepo.findByEventIdOrderByAttendedAtAsc(eventId).stream()
        .map(EventController::toDto)
        .toList();
  }

  @PutMapping("/{eventId}/observations")
  @Transactional
  public EventDto updateEventObservations(
      @PathVariable Long eventId,
      @Valid @RequestBody UpdateEventObservationsRequest req,
      HttpServletRequest request
  ) {
    requireViewer(request);

    var e = eventRepo.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    if (!e.isActive()) throw new IllegalArgumentException("Evento no encontrado");

    var obs = req.observations();
    e.setObservations(obs == null ? null : obs.trim());
    e = eventRepo.save(e);

    return toDto(e);
  }

  @PutMapping("/attendance/{attendanceId}")
  @Transactional
  public AttendanceDto updateAttendance(
      @PathVariable Long attendanceId,
      @Valid @RequestBody UpdateAttendanceRequest req,
      HttpServletRequest request
  ) {
    requireViewer(request);

    // Body and path must match (boring but safer).
    var bodyId = parseLong(req.attendanceId(), "Datos inválidos");
    if (!attendanceId.equals(bodyId)) {
      throw new IllegalArgumentException("Datos inválidos");
    }

    var a = attendanceRepo.findById(attendanceId)
        .orElseThrow(() -> new IllegalArgumentException("Asistencia no encontrada"));

    var obs = req.observations();
    a.setObservations(obs == null ? null : obs.trim());
    a = attendanceRepo.save(a);

    return toDto(a);
  }

  private static EventDto toDto(EventEntity e) {
    return new EventDto(
        String.valueOf(e.getId()),
        e.getTitle(),
        String.valueOf(e.getAreaId()),
        e.getStartDate().toString(),
        e.getEndDate().toString(),
        String.valueOf(e.getCreatedByPersonId()),
        e.getObservations(),
        e.isActive()
    );
  }

  private static AttendanceDto toDto(AttendanceEntity a) {
    return new AttendanceDto(
        String.valueOf(a.getId()),
        String.valueOf(a.getEventId()),
        String.valueOf(a.getPersonId()),
        a.getAttendedAt().withNano(0).toString() + "Z",
        a.getObservations()
    );
  }

  private static Long parseLong(String value, String errorMsg) {
    try {
      return Long.parseLong(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  private static LocalDate parseDate(String value, String errorMsg) {
    try {
      return LocalDate.parse(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  private static CurrentUser requireViewer(HttpServletRequest request) {
    var viewer = (CurrentUser) request.getAttribute(AuthTokenFilter.ATTR_CURRENT_USER);
    if (viewer == null) {
      throw new SecurityException("No autenticado");
    }
    return viewer;
  }
}
