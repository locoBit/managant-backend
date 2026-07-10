package com.managant.backend.api.dto;

import java.util.List;

public record AreaAttendanceReportDto(
    String areaId,
    String startDate,
    String endDate,
    String totalEvents,
    List<AreaAttendanceRowDto> rows
) {}
