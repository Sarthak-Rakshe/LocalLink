package com.sarthak.ReviewService.dto;

import lombok.Builder;

@Builder
public record ReviewAggregateResponse(
        Long aggregateId,
        Long serviceProviderId,
        Long serviceId,
        Double averageRating,
        Long totalReviews
) {}
