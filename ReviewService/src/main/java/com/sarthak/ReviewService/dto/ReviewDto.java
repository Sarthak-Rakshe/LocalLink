package com.sarthak.ReviewService.dto;

import lombok.Builder;

@Builder
public record ReviewDto (
    Long reviewId,
    Long serviceProviderId,
    Long serviceId,
    Long bookingId,
    Long customerId,
    Integer rating,
    String comment,
    String createdAt,
    String updatedAt
){}
