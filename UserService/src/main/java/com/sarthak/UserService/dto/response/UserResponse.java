package com.sarthak.UserService.dto.response;

import com.sarthak.UserService.dto.ProviderReviewAggregateResponse;
import lombok.Builder;


@Builder
public record UserResponse (
        Long userId,
        String userName,
        String userEmail,
        String userContact,
        String userType,
        String userAddress,
        boolean isActive
){}
