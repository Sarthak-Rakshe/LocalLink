package com.sarthak.BookingService.dto;

public record QueryFilter (
        String bookingStatus,
        String serviceCategory,
        String dateFrom,
        String dateTo,
        Long serviceProviderId,
        Long customerId,
        Long serviceId
){}
