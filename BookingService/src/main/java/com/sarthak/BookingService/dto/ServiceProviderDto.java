package com.sarthak.BookingService.dto;

public record ServiceProviderDto(
        Long serviceProviderId,
        String serviceProviderName,
        String serviceProviderContact
) {}
