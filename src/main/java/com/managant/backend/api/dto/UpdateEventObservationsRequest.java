package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateEventObservationsRequest(
    @NotBlank(message = "observations requerido") String observations
) {}
