package com.managant.backend.api.dto;

import java.util.List;

public record AreaDto(
    String id,
    String name,
    String categoryId,
    String parentAreaId,
    String responsiblePersonId,
    String responsibleName,
    String helperPersonId,
    String helperName,
    List<String> allowedRoleIds,
    boolean active
) {}
