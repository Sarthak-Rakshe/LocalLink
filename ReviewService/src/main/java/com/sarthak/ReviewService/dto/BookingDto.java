package com.sarthak.ReviewService.dto;

public record BookingDto(
        Long bookingId,
        Long customerId,
        Long serviceId,
        Long serviceProviderId,
        String bookingDate,
        String bookingStartTime,
        String bookingEndTime,
        String bookingStatus,
        String createdAt,
        String rescheduledToId
) {
}
