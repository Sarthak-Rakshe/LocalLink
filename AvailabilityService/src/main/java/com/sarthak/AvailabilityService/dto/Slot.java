package com.sarthak.AvailabilityService.dto;

import java.time.LocalTime;

public record Slot(
        LocalTime startTime,
        LocalTime endTime
) {}
