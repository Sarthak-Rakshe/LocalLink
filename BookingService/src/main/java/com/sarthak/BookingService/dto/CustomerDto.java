package com.sarthak.BookingService.dto;

public record CustomerDto(
        Long customerId,
        String customerName,
        String customerContact
) {}
