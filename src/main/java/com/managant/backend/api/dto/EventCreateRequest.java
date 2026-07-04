package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EventCreateRequest(
    @NotBlank(message = "El título es obligatorio") String title,
    @NotBlank(message = "areaId requerido") String areaId,
    @NotBlank(message = "startDate requerido") String startDate,
    @NotBlank(message = "endDate requerido") String endDate
) {}
