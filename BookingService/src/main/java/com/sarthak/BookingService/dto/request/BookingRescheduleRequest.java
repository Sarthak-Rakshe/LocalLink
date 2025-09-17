package com.sarthak.BookingService.dto.request;

import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record BookingRescheduleRequest(
        Long bookingId,
        LocalDate newBookingDate,
        LocalTime newBookingStartTime,
        LocalTime newBookingEndTime
) {
}
