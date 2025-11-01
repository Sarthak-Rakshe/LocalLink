package com.sarthak.ServiceListingService.dto;

public record QueryFilter(
        String category,
        Long serviceProviderId,
        String serviceName,
        Double minPrice,
        Double maxPrice
) {}
