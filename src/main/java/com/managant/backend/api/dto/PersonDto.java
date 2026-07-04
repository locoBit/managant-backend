package com.managant.backend.api.dto;

import java.util.List;

public record PersonDto(
    String id,
    String firstNames,
    String lastName,
    String motherLastName,
    String birthDate,
    String curp,
    List<PersonMembershipDto> areas,
    boolean active
) {}
