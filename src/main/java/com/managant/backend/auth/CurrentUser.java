package com.managant.backend.auth;

public record CurrentUser(
    Long userId,
    String username,
    Long personId,
    boolean isAdmin
) {}
