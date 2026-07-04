package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Backward-compatible DTO to avoid changing the frontend payload shape too much.
 * We ignore createdByPersonId server-side.
 */
public record EventCreateCompatRequest(
    @NotBlank(message = "El título es obligatorio") String title,
    @NotBlank(message = "areaId requerido") String areaId,
    @NotBlank(message = "startDate requerido") String startDate,
    @NotBlank(message = "endDate requerido") String endDate,
    String createdByPersonId
) {}
