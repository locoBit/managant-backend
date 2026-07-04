package com.managant.backend.api.dto;

/**
 * Tells the backend whether sub-areas are included. This matches the frontend checkbox.
 */
public record AttendanceRuleDto(boolean includeSubareas) {}
