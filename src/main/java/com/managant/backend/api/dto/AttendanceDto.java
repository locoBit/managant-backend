package com.managant.backend.api.dto;

public record AttendanceDto(
    String id,
    String eventId,
    String personId,
    String attendedAt,
    String observations
) {}
