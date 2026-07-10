package com.managant.backend.api.dto;

public record ViewerDto(
    String userId,
    String username,
    String name,
    String personId,
    boolean isAdmin
) {}
