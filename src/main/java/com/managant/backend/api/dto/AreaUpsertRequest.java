package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AreaUpsertRequest(
    @NotBlank(message = "El nombre es obligatorio") String name,
    String categoryId,
    String parentAreaId,
    @NotBlank(message = "Debes seleccionar un responsable") String responsiblePersonId,
    String helperPersonId,
    @NotEmpty(message = "Debes seleccionar al menos un rol permitido") List<String> allowedRoleIds,
    Boolean active
) {}
