package com.sarthak.BookingService.dto;

import java.util.Set;

public record ServiceListingQueryFilter(
        Set<Long> serviceIdSet,
        String category,
        Long serviceProviderId,
        String serviceName,
        Double minPrice,
        Double maxPrice
) {}
