package com.managant.backend.http;

import com.managant.backend.api.dto.AreaCategoryCreateRequest;
import com.managant.backend.api.dto.AreaCategoryDto;
import com.managant.backend.api.dto.AreaDto;
import com.managant.backend.api.dto.AreaUpsertRequest;
import com.managant.backend.api.dto.AssignMembershipRequest;
import com.managant.backend.api.dto.ChangeMembershipRoleRequest;
import com.managant.backend.api.dto.UnassignMembershipRequest;
import com.managant.backend.api.dto.AttendanceDto;
import com.managant.backend.api.dto.EventCreateCompatRequest;
import com.managant.backend.api.dto.EventCreateRequest;
import com.managant.backend.api.dto.EventDto;
import com.managant.backend.api.dto.PersonDto;
import com.managant.backend.api.dto.PersonUpsertRequest;
import com.managant.backend.api.dto.RegisterAttendanceRequest;
import com.managant.backend.api.dto.UpdateAttendanceRequest;
import com.managant.backend.api.dto.UpdateEventObservationsRequest;
import com.managant.backend.api.dto.UpsertAttendanceRequest;
import com.managant.backend.api.dto.RoleDto;
import com.managant.backend.api.dto.RoleUpsertRequest;
import com.managant.backend.api.dto.UserDto;
import com.managant.backend.api.dto.UserUpsertRequest;
import com.managant.backend.api.AreaCategoryController;
import com.managant.backend.api.AreaController;
import com.managant.backend.api.EventController;
import com.managant.backend.api.PersonController;
import com.managant.backend.api.RoleController;
import com.managant.backend.api.UserController;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Compatibility layer so the existing frontend (built around fakeApi function names)
 * can call the real backend without rewriting everything today.
 *
 * Later: delete this and move frontend to clean REST routes.
 */
@RestController
@RequestMapping("/api/compat")
public class FrontendCompatController {

  private final RoleController roleController;
  private final PersonController personController;
  private final AreaController areaController;
  private final AreaCategoryController areaCategoryController;
  private final UserController userController;
  private final EventController eventController;

  public FrontendCompatController(
      RoleController roleController,
      PersonController personController,
      AreaController areaController,
      AreaCategoryController areaCategoryController,
      UserController userController,
      EventController eventController
  ) {
    this.roleController = roleController;
    this.personController = personController;
    this.areaController = areaController;
    this.areaCategoryController = areaCategoryController;
    this.userController = userController;
    this.eventController = eventController;
  }

  // Roles
  @GetMapping("/roles")
  public List<RoleDto> listRoles(jakarta.servlet.http.HttpServletRequest request) {
    return roleController.list(request);
  }

  @PostMapping("/roles")
  public RoleDto createRole(@Valid @RequestBody RoleUpsertRequest req) {
    return roleController.create(req);
  }

  @PutMapping("/roles/{id}")
  public RoleDto updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpsertRequest req) {
    return roleController.update(id, req);
  }

  @DeleteMapping("/roles/{id}")
  public void deleteRole(@PathVariable Long id) {
    roleController.delete(id);
  }

  // People
  @GetMapping("/people")
  public List<PersonDto> listPeople(jakarta.servlet.http.HttpServletRequest request) {
    return personController.list(request);
  }

  @PostMapping("/people")
  public PersonDto createPerson(@Valid @RequestBody PersonUpsertRequest req) {
    return personController.create(req);
  }

  @PutMapping("/people/{id}")
  public PersonDto updatePerson(@PathVariable Long id, @Valid @RequestBody PersonUpsertRequest req) {
    return personController.update(id, req);
  }

  @PostMapping("/people/assign")
  public PersonDto assignMembership(@Valid @RequestBody AssignMembershipRequest req) {
    return personController.assignMembership(req);
  }

  @PostMapping("/people/change-role")
  public PersonDto changeRole(@Valid @RequestBody ChangeMembershipRoleRequest req) {
    return personController.changeRole(req);
  }

  @PostMapping("/people/unassign")
  public PersonDto unassignMembership(@Valid @RequestBody UnassignMembershipRequest req) {
    return personController.unassignMembership(req);
  }

  @PostMapping("/people/{id}/deactivate")
  public com.managant.backend.api.dto.SuccessDto deactivatePerson(@PathVariable Long id) {
    return personController.deactivate(id);
  }

  // Areas
  @GetMapping("/areas")
  public List<AreaDto> listAreas(jakarta.servlet.http.HttpServletRequest request) {
    return areaController.list(request);
  }

  @PostMapping("/areas")
  public AreaDto createArea(@Valid @RequestBody AreaUpsertRequest req) {
    return areaController.create(req);
  }

  @PutMapping("/areas/{id}")
  public AreaDto updateArea(@PathVariable Long id, @Valid @RequestBody AreaUpsertRequest req) {
    return areaController.update(id, req);
  }

  // Area categories
  @GetMapping("/area-categories")
  public List<AreaCategoryDto> listAreaCategories(jakarta.servlet.http.HttpServletRequest request) {
    return areaCategoryController.list(request);
  }

  @PostMapping("/area-categories")
  public AreaCategoryDto createAreaCategory(@Valid @RequestBody AreaCategoryCreateRequest req) {
    return areaCategoryController.create(req);
  }

  // Users
  @GetMapping("/users")
  public List<UserDto> listUsers(jakarta.servlet.http.HttpServletRequest request) {
    return userController.list(request);
  }

  @PostMapping("/users")
  public UserDto upsertUser(@Valid @RequestBody UserUpsertRequest req) {
    return userController.upsert(req);
  }

  // Events
  @GetMapping("/events")
  public List<EventDto> listEvents(jakarta.servlet.http.HttpServletRequest request) {
    return eventController.list(request);
  }

  @PostMapping("/events")
  public EventDto createEvent(
      @Valid @RequestBody EventCreateCompatRequest req,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    // Ignore createdByPersonId, derived from token.
    return eventController.create(
        new EventCreateRequest(req.title(), req.areaId(), req.startDate(), req.endDate()),
        request
    );
  }

  @PostMapping("/events/attendance")
  public AttendanceDto registerAttendance(
      @Valid @RequestBody RegisterAttendanceRequest req,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    // keep the compat path, but now we support includeSubareas in body
    return eventController.registerAttendance(req, request);
  }

  @PostMapping("/events/attendance/upsert")
  public AttendanceDto upsertAttendance(
      @Valid @RequestBody UpsertAttendanceRequest req,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    return eventController.upsertAttendance(req, request);
  }

  @PutMapping("/events/{eventId}/observations")
  public EventDto updateEventObservations(
      @PathVariable Long eventId,
      @Valid @RequestBody UpdateEventObservationsRequest req,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    return eventController.updateEventObservations(eventId, req, request);
  }

  @PutMapping("/events/attendance/{attendanceId}")
  public AttendanceDto updateAttendance(
      @PathVariable Long attendanceId,
      @Valid @RequestBody UpdateAttendanceRequest req,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    return eventController.updateAttendance(attendanceId, req, request);
  }

  @GetMapping("/events/{eventId}/attendance")
  public List<AttendanceDto> listAttendance(@PathVariable Long eventId, jakarta.servlet.http.HttpServletRequest request) {
    return eventController.listAttendance(eventId, request);
  }
}
