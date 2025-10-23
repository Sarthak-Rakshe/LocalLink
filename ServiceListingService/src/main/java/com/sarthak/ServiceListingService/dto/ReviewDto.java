package com.sarthak.ServiceListingService.dto;

import lombok.Builder;

@Builder
public record ReviewDto(
    Long reviewId,
    Long serviceProviderId,
    Long serviceId,
    Long customerId,
    Integer rating,
    String comment,
    String createdAt,
    String updatedAt
){}
