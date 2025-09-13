package com.sarthak.ReviewService.controller;

import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{review-id}")
    public ResponseEntity<ReviewDto> getById(@PathVariable Long reviewId){
        ReviewDto review = reviewService.getById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/{service-id}")
    public Page<ReviewDto> getReviewsForServiceProvider(
            @PathVariable("service-provider-id") Long serviceId,
            int page,
            int size
    ){
        return reviewService.getReviewsForService(serviceId, page, size);
    }

    @GetMapping("/customers/{customer-id}")
    public Page<ReviewDto> getReviewsByCustomer(
            @PathVariable("customer-id") Long customerId,
            int page,
            int size
    ){
        return reviewService.getReviewsForCustomer(customerId, page, size);
    }

    @GetMapping("/service-providers/{service-provider-id}/average-rating")
    public ResponseEntity<Double> getAverageRatingForServiceProvider(
            @PathVariable("service-provider-id") Long serviceProviderId
    ) {
        Double avgRating = reviewService.calculateAverageRatingForServiceProvider(serviceProviderId);
        return ResponseEntity.ok(avgRating);
    }

    @GetMapping("/services/{service-id}/average-rating")
    public ResponseEntity<Double> getAverageRatingForService(
            @PathVariable("service-id") Long serviceId
    ) {
        Double avgRating = reviewService.getAverageRatingForService(serviceId);
        return ResponseEntity.ok(avgRating);
    }

    @PostMapping()
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewDto reviewDto){
        ReviewDto saved = reviewService.addReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
