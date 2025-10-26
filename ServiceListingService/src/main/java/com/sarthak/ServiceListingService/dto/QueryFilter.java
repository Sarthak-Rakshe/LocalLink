package com.sarthak.ServiceListingService.dto;

public record QueryFilter(
        String category,
        Long userId,
        String serviceName,
        Double minPrice,
        Double maxPrice
) {}
