package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignMembershipRequest(
    @NotBlank(message = "personId requerido") String personId,
    @NotBlank(message = "areaId requerido") String areaId,
    @NotBlank(message = "roleId requerido") String roleId
) {}
