package com.managant.backend.api.dto;

public record EventDto(
    String id,
    String title,
    String areaId,
    String startDate,
    String endDate,
    String createdByPersonId,
    String observations,
    boolean active
) {}
