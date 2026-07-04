package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpsertAttendanceRequest(
    @NotBlank(message = "eventId requerido") String eventId,
    @NotBlank(message = "personId requerido") String personId,
    Boolean present,
    Boolean includeSubareas,
    String observations
) {}
