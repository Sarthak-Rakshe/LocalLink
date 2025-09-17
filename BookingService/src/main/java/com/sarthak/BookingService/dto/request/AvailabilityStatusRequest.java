package com.sarthak.BookingService.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record AvailabilityStatusRequest (
        Long serviceProviderId,
        Long serviceId,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate date
) {
}
