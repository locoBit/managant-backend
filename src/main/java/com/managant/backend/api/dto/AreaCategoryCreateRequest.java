package com.managant.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AreaCategoryCreateRequest(
    @NotBlank(message = "El nombre de la categoría es obligatorio") String name
) {}
