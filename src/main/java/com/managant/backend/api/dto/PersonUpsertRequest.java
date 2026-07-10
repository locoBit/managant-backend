package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record PersonUpsertRequest(
    @NotBlank(message = "Nombre es obligatorio") String firstNames,
    @NotBlank(message = "Apellido paterno es obligatorio") String lastName,
    String motherLastName,
    @NotBlank(message = "Fecha de nacimiento es obligatoria") String birthDate,

    // Optional and encrypted at rest.
    // Note: we intentionally do NOT validate format too hard here (different countries).
    String phoneNumber,

    // Optional: if provided on CREATE, the backend will assign this role in the root area.
    // This lets the frontend create a person + role in one call.
    String initialRoleId
) {}
