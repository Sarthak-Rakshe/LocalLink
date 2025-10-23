package com.sarthak.UserService.dto;

import lombok.Builder;

@Builder
public record ProviderReviewAggregateResponse(
        Long serviceProviderId,
        Double averageRating,
        Long totalReviews
) {
}
