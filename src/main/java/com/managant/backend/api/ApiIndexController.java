package com.managant.backend.api;

import com.managant.backend.api.dto.ApiEndpointsDto;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiIndexController {

  @GetMapping
  public ApiEndpointsDto index() {
    return new ApiEndpointsDto(Map.of(
        "health", "/api/health",
        "login", "/api/auth/login",
        "roles", "/api/roles",
        "people", "/api/people",
        "areas", "/api/areas",
        "areaCategories", "/api/area-categories",
        "users", "/api/users",
        "events", "/api/events",
        "eventAttendanceList", "/api/events/{eventId}/attendance",
        "eventAttendanceRegister", "/api/events/attendance"
    ));
  }
}
