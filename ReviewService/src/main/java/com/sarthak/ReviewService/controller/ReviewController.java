package com.sarthak.ReviewService.controller;

import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
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
    public ResponseEntity<ReviewDto> getById(@PathVariable("review-id") Long reviewId){
        ReviewDto review = reviewService.getById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/{service-id}/service")
    public Page<ReviewDto> getReviewsForService(
            @PathVariable("service-id") Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return reviewService.getReviewsForService(serviceId, page, size);
    }

    @GetMapping("/{customer-id}/customers")
    public Page<ReviewDto> getReviewsByCustomer(
            @PathVariable("customer-id") Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return reviewService.getReviewsForCustomer(customerId, page, size);
    }


    @PostMapping()
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewDto reviewDto){
        ReviewDto saved = reviewService.addReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{review-id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable("review-id") Long reviewId, @RequestBody ReviewDto reviewDto){
        ReviewDto updated = reviewService.updateReview(reviewId, reviewDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{review-id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("review-id") Long reviewId){
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/{service-id}/average")
    public ResponseEntity<Double> getAverageRatingForService(@PathVariable("service-id") Long serviceId){
        Double averageRating = reviewService.getAverageRatingForService(serviceId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/providers/{service-provider-id}/aggregate")
    public ResponseEntity<ProviderReviewAggregateResponse> getAggregateReviewsForServiceProvider(
            @PathVariable("service-provider-id") Long serviceProviderId
    ){
        return ResponseEntity.ok(reviewService.getReviewAggregateForProvider(serviceProviderId));
    }

}
