package com.sarthak.ReviewService.mapper;

import com.sarthak.ReviewService.dto.ReviewAggregateResponse;
import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
import com.sarthak.ReviewService.model.Review;
import com.sarthak.ReviewService.model.ReviewAggregate;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ReviewMapper {

    public ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .serviceProviderId(review.getServiceProviderId())
                .serviceId(review.getServiceId())
                .customerId(review.getCustomerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt().toString())
                .updatedAt(review.getUpdatedAt().toString())
                .build();
    }

    public Review mapToEntity(ReviewDto reviewDto) {
        return Review.builder()
                .serviceProviderId(reviewDto.serviceProviderId())
                .serviceId(reviewDto.serviceId())
                .customerId(reviewDto.customerId())
                .rating(reviewDto.rating())
                .comment(reviewDto.comment())
                .build();
    }

    public ReviewAggregateResponse mapToAggregateResponse(ReviewAggregate reviewAggregate) {
        return ReviewAggregateResponse.builder()
                .aggregateId(reviewAggregate.getAggregateId())
                .serviceProviderId(reviewAggregate.getServiceProviderId())
                .serviceId(reviewAggregate.getServiceId())
                .averageRating(reviewAggregate.getAverageRating())
                .totalReviews(reviewAggregate.getTotalReviews())
                .build();
    }

    public List<ReviewAggregateResponse> mapToAggregateResponseList(List<ReviewAggregate> reviewAggregates) {
        return reviewAggregates.stream()
                .map(this::mapToAggregateResponse)
                .toList();
    }
}
