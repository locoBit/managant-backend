package com.managant.backend.api.dto;

public record PersonSearchDto(
    String id,
    String label,
    String firstNames,
    String lastName,
    String motherLastName
) {}
