package com.sarthak.UserService.dto.response;

import com.sarthak.UserService.dto.ProviderReviewAggregateResponse;
import lombok.Builder;

@Builder
public record ProviderResponse (
        Long providerId,
        String providerName,
        String providerContact,
        String providerEmail,
        String providerAddress,
        boolean isActive,
        ProviderReviewAggregateResponse providerReviewAggregateResponse
){}
