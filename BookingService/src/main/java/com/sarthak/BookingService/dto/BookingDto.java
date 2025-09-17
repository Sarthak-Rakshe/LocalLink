package com.sarthak.BookingService.dto;


import com.sarthak.BookingService.model.BookingStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
public record BookingDto (
        Long bookingId,
        Long customerId,
        Long serviceId,
        Long serviceProviderId,
        String bookingDate,
        String bookingStartTime,
        String bookingEndTime,
        BookingStatus bookingStatus,
        String createdAt,
        String rescheduledToId
){}
