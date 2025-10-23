package com.sarthak.ReviewService.dto.response;

import lombok.Builder;

@Builder
public record ProviderReviewAggregateResponse(
        Long serviceProviderId,
        Double averageRating,
        Long totalReviews
) {
}
