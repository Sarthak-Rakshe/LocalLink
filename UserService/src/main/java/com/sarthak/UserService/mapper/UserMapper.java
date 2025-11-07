package com.sarthak.UserService.mapper;

import com.sarthak.UserService.dto.ProviderReviewAggregateResponse;
import com.sarthak.UserService.dto.response.ProviderResponse;
import com.sarthak.UserService.dto.response.UserResponse;
import com.sarthak.UserService.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .userEmail(user.getUserEmail())
                .userContact(user.getUserContact())
                .userType(user.getUserType().name())
                .userAddress(user.getUserAddress())
                .isActive(user.getIsActive())
                .build();
    }

    public ProviderResponse toProviderResponse(User user, ProviderReviewAggregateResponse reviewAggregate) {
        if(reviewAggregate == null){
            reviewAggregate = ProviderReviewAggregateResponse.builder()
                    .serviceProviderId(user.getUserId())
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .build();
        }
        return ProviderResponse.builder()
                .providerId(user.getUserId())
                .providerName(user.getUsername())
                .providerEmail(user.getUserEmail())
                .providerContact(user.getUserContact())
                .providerAddress(user.getUserAddress())
                .providerReviewAggregateResponse(reviewAggregate)
                .build();
    }
}
