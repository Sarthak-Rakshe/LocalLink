package com.sarthak.ReviewService.dto.response;

import lombok.Builder;

@Builder
public record ProviderReviewAggregateResponse(
        Double averageRating,
        Long totalReviews
) {
}
