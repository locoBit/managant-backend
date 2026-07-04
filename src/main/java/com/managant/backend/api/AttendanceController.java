package com.managant.backend.api;

import com.managant.backend.api.dto.AttendanceDto;
import com.managant.backend.api.dto.AttendanceRuleDto;
import com.managant.backend.api.dto.RegisterAttendanceRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Separate controller so we can pass query params like includeSubareas=true.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

  private final EventController eventController;

  public AttendanceController(EventController eventController) {
    this.eventController = eventController;
  }

  @PostMapping
  public AttendanceDto register(
      @Valid @RequestBody RegisterAttendanceRequest req,
      @RequestParam(name = "includeSubareas", required = false) Boolean includeSubareas,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    // Prefer query param if present (cleaner). Fallback to body field.
    Boolean resolved = includeSubareas != null ? includeSubareas : req.includeSubareas();
    return eventController.registerAttendance(new RegisterAttendanceRequest(req.eventId(), req.personId(), resolved), request);
  }

  @GetMapping("/{eventId}")
  public List<AttendanceDto> list(@PathVariable Long eventId, jakarta.servlet.http.HttpServletRequest request) {
    return eventController.listAttendance(eventId, request);
  }

  @GetMapping("/rule")
  public AttendanceRuleDto rule() {
    // default behavior for backend: exact area only
    return new AttendanceRuleDto(false);
  }
}
