package com.sarthak.BookingService.dto;

public record ServiceDto(
        Long serviceId,
        String serviceName,
        String serviceCategory,
        double servicePricePerHour
) {}
