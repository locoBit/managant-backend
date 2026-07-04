package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeMembershipRoleRequest(
    @NotBlank(message = "personId requerido") String personId,
    @NotBlank(message = "areaId requerido") String areaId,
    @NotBlank(message = "fromRoleId requerido") String fromRoleId,
    @NotBlank(message = "toRoleId requerido") String toRoleId
) {}
