package com.sarthak.BookingService.dto;

import com.sarthak.BookingService.model.BookingStatus;
import lombok.Builder;

@Builder
public record BookingDto (
        Long bookingId,
        Long customerId,
        Long serviceId,
        String serviceCategory,
        Long serviceProviderId,
        String bookingDate,
        String bookingStartTime,
        String bookingEndTime,
        BookingStatus bookingStatus,
        String createdAt,
        String rescheduledToId
){}
