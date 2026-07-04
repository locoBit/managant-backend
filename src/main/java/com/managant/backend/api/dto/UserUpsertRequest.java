package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpsertRequest(
    @NotBlank(message = "Persona requerida") String personId,
    @NotBlank(message = "El correo es obligatorio") String gmail,
    Boolean active
) {}
