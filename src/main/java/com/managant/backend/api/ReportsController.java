package com.managant.backend.api;

import com.managant.backend.api.dto.AreaAttendanceReportDto;
import com.managant.backend.api.dto.AreaAttendanceRowDto;
import com.managant.backend.auth.AuthTokenFilter;
import com.managant.backend.auth.CurrentUser;
import com.managant.backend.db.AreaRepository;
import com.managant.backend.db.AttendanceRepository;
import com.managant.backend.db.EventRepository;
import com.managant.backend.db.PersonMembershipRepository;
import com.managant.backend.db.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

  private final AreaRepository areaRepo;
  private final EventRepository eventRepo;
  private final AttendanceRepository attendanceRepo;
  private final PersonMembershipRepository membershipRepo;
  private final PersonRepository personRepo;

  public ReportsController(
      AreaRepository areaRepo,
      EventRepository eventRepo,
      AttendanceRepository attendanceRepo,
      PersonMembershipRepository membershipRepo,
      PersonRepository personRepo
  ) {
    this.areaRepo = areaRepo;
    this.eventRepo = eventRepo;
    this.attendanceRepo = attendanceRepo;
    this.membershipRepo = membershipRepo;
    this.personRepo = personRepo;
  }

  @GetMapping("/area-attendance")
  public AreaAttendanceReportDto areaAttendance(
      @RequestParam(name = "areaId") Long areaId,
      @RequestParam(name = "start", required = false) String start,
      @RequestParam(name = "end", required = false) String end,
      HttpServletRequest request
  ) {
    var viewer = (CurrentUser) request.getAttribute(AuthTokenFilter.ATTR_CURRENT_USER);
    if (viewer == null) throw new SecurityException("No autenticado");

    var allAreas = areaRepo.findAll();
    ensureViewerCanManageAreaOrAncestor(viewer, areaId, allAreas);

    LocalDate startDate = start == null || start.isBlank() ? LocalDate.now().minusDays(90) : parseDate(start);
    LocalDate endDate = end == null || end.isBlank() ? LocalDate.now() : parseDate(end);
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("Rango de fechas inválido");
    }

    // Events: only selected area.
    var events = eventRepo.findAll().stream()
        .filter(e -> e.isActive() && areaId.equals(e.getAreaId()))
        .filter(e -> !e.getStartDate().isBefore(startDate) && !e.getStartDate().isAfter(endDate))
        .toList();

    var eventIds = events.stream().map(x -> x.getId()).collect(Collectors.toSet());

    // People: members assigned to THIS area.
    var memberPersonIds = membershipRepo.findAll().stream()
        .filter(m -> areaId.equals(m.getAreaId()))
        .map(m -> m.getPersonId())
        .collect(Collectors.toSet());

    var people = personRepo.findAll().stream()
        .filter(p -> p.isActive())
        .filter(p -> memberPersonIds.contains(p.getId()))
        .toList();

    Set<Long> personIds = people.stream().map(p -> p.getId()).collect(Collectors.toSet());

    Map<Long, Long> attendedCountByPersonId = new HashMap<>();
    Map<Long, String> lastAttendedAtByPersonId = new HashMap<>();

    if (!eventIds.isEmpty() && !personIds.isEmpty()) {
      for (var row : attendanceRepo.countByEventIdsAndPersonIds(eventIds, personIds)) {
        attendedCountByPersonId.put(row.getPersonId(), row.getCnt());
      }

      for (var row : attendanceRepo.maxAttendedAtByEventIdsAndPersonIds(eventIds, personIds)) {
        lastAttendedAtByPersonId.put(
            row.getPersonId(),
            row.getLastAttendedAt() == null ? null : row.getLastAttendedAt().toString()
        );
      }
    }

    long totalEvents = events.size();

    var rows = people.stream()
        .map(p -> {
          long attended = attendedCountByPersonId.getOrDefault(p.getId(), 0L);
          int pct = totalEvents <= 0 ? 0 : (int) Math.round((attended * 100.0) / totalEvents);
          return new AreaAttendanceRowDto(
              String.valueOf(p.getId()),
              (p.getFirstNames() + " " + p.getLastName() + " " + p.getMotherLastName()).trim(),
              String.valueOf(attended),
              String.valueOf(totalEvents),
              attended + "/" + totalEvents,
              pct,
              lastAttendedAtByPersonId.get(p.getId())
          );
        })
        .sorted(Comparator.comparingInt(AreaAttendanceRowDto::percentage).reversed()
            .thenComparing(AreaAttendanceRowDto::fullName))
        .toList();

    return new AreaAttendanceReportDto(
        String.valueOf(areaId),
        startDate.toString(),
        endDate.toString(),
        String.valueOf(totalEvents),
        rows
    );
  }

  private static LocalDate parseDate(String s) {
    try {
      return LocalDate.parse(s.trim());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Fecha inválida");
    }
  }

  private static void ensureViewerCanManageAreaOrAncestor(CurrentUser viewer, Long areaId, List<com.managant.backend.db.AreaEntity> allAreas) {
    if (viewer.isAdmin()) return;

    Map<Long, com.managant.backend.db.AreaEntity> map = allAreas.stream()
        .collect(Collectors.toMap(com.managant.backend.db.AreaEntity::getId, x -> x));

    Long cur = areaId;
    while (cur != null) {
      var a = map.get(cur);
      if (a == null) break;
      if (!a.isActive()) break;

      if (viewer.personId().equals(a.getResponsiblePersonId())
          || (a.getHelperPersonId() != null && viewer.personId().equals(a.getHelperPersonId()))) {
        return;
      }

      cur = a.getParentAreaId();
    }

    throw new SecurityException("No autorizado");
  }
}
