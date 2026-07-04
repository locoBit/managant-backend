package com.managant.backend.auth.dto;

public record LoginResponse(
    String id,
    String username,
    String name,
    String personId,
    boolean isAdmin,
    String token
) {}
