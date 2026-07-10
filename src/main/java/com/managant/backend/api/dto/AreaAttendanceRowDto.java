package com.managant.backend.api.dto;

public record AreaAttendanceRowDto(
    String personId,
    String fullName,
    String attended,
    String total,
    String fraction,
    int percentage,
    String lastAttendedAt
) {}
