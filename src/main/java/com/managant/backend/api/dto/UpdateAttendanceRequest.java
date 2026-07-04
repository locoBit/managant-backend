package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAttendanceRequest(
    @NotBlank(message = "attendanceId requerido") String attendanceId,
    String observations
) {}
