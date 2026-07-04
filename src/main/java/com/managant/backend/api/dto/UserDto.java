package com.managant.backend.api.dto;

public record UserDto(String id, String personId, String username, String password, boolean active) {}
