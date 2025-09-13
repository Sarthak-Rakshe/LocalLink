package com.sarthak.ReviewService.mapper;

import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.model.Review;
import org.springframework.stereotype.Component;

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
                .build();
    }

    public Review mapToEntity(ReviewDto reviewDto) {
        return Review.builder()
                .reviewId(reviewDto.reviewId())
                .serviceProviderId(reviewDto.serviceProviderId())
                .serviceId(reviewDto.serviceId())
                .customerId(reviewDto.customerId())
                .rating(reviewDto.rating())
                .comment(reviewDto.comment())
                .build();
    }
}
