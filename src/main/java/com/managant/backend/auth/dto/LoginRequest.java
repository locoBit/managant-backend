package com.managant.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "idToken requerido") String idToken
) {}
