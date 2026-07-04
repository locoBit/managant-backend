package com.managant.backend.api.dto;

public record RoleDto(
    String id,
    String name,
    String scope,
    int maxPeople,
    boolean active
) {}
