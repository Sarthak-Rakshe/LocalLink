package com.sarthak.AvailabilityService.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DayAndTimeAvailabilityRequest(
        DayOfWeek day,
        LocalTime startTime,
        LocalTime endTime
) {
}
