package com.managant.backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoleUpsertRequest(
    @NotBlank(message = "El nombre es obligatorio") String name,
    @NotNull(message = "El scope es obligatorio") String scope,
    @Min(value = 0, message = "maxPeople debe ser >= 0") int maxPeople,
    Boolean active
) {}
