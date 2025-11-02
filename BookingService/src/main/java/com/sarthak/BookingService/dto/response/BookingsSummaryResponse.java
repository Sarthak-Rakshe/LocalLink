package com.sarthak.BookingService.dto.response;

import lombok.Builder;

@Builder
public record BookingsSummaryResponse(
    Long requesterId,
    Long totalBookings,
    Long completedBookings,
    Long confirmedBookings,
    Long cancelledBookings,
    Long pendingBookings,
    Long rescheduledBookings,
    Long deletedBookings
) {}

