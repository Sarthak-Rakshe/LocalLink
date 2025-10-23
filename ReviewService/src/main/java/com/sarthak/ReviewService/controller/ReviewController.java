package com.sarthak.ReviewService.controller;

import com.sarthak.ReviewService.config.shared.UserPrincipal;
import com.sarthak.ReviewService.dto.ReviewAggregateResponse;
import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.dto.response.PagedResponse;
import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
import com.sarthak.ReviewService.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/serviceProvider/myReviews")
    @PreAuthorize("principal.userType.equals('PROVIDER')")
    public PagedResponse<ReviewDto> getMyReviews(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<ReviewDto> review = reviewService.getReviewsByServiceProviderId(userPrincipal.getUserId(), page, size, sortBy, sortDir);
        return new PagedResponse<ReviewDto>(
                review.getContent(),
                review.getNumber(),
                review.getSize(),
                review.getTotalElements(),
                review.getTotalPages()
        );
    }

    @GetMapping("/{serviceId}/service")
    public PagedResponse<ReviewDto> getReviewsForService(
            @PathVariable("serviceId") Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ){
        Page<ReviewDto> reviewDtos = reviewService.getReviewsForService(serviceId, page, size, sortBy, sortDir);
        return new PagedResponse<>(
                reviewDtos.getContent(),
                reviewDtos.getNumber(),
                reviewDtos.getSize(),
                reviewDtos.getTotalElements(),
                reviewDtos.getTotalPages()
        );
    }

    @GetMapping("/customer/myReviews")
    @PreAuthorize("principal.userType.equals('CUSTOMER')")
    public PagedResponse<ReviewDto> getReviewsByCustomer(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir
    ){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Page<ReviewDto> reviewDtos = reviewService.getReviewsForCustomer(userPrincipal.getUserId(), page, size, sortBy, sortDir);
        return new PagedResponse<>(
                reviewDtos.getContent(),
                reviewDtos.getNumber(),
                reviewDtos.getSize(),
                reviewDtos.getTotalElements(),
                reviewDtos.getTotalPages()
        );
    }


    @PostMapping()
    @PreAuthorize("principal.userType.equals('CUSTOMER')")
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewDto reviewDto){
        ReviewDto saved = reviewService.addReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("principal.userType.equals('CUSTOMER')")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable("reviewId") Long reviewId,
                                                  @RequestBody ReviewDto reviewDto, Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ReviewDto updated = reviewService.updateReview(reviewId, reviewDto, userPrincipal.getUserId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("principal.userType.equals('CUSTOMER')")
    public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") Long reviewId,
                                             Authentication authentication){
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        reviewService.deleteReview(reviewId, userPrincipal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/{serviceId}/average")
    public ResponseEntity<Double> getAverageRatingForService(@PathVariable("serviceId") Long serviceId){
        Double averageRating = reviewService.getAverageRatingForService(serviceId);
        return ResponseEntity.ok(averageRating);
    }

    @PostMapping("/providers/aggregate")
    public ResponseEntity<Map<Long, ProviderReviewAggregateResponse>> getAggregateReviewsForServiceProvider(
            @RequestBody List<Long> serviceProviderIds
    ){
        return ResponseEntity.ok(reviewService.getReviewAggregateForProvider(serviceProviderIds));
    }

    @PostMapping("/getByServiceIds")
    public ResponseEntity<List<ReviewDto>> getByServiceIds(@RequestBody List<Long> serviceIds){
        List<ReviewDto> reviews = reviewService.getByServiceIds(serviceIds);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/getAggregatesByServiceIds")
    public ResponseEntity<Map<Long, ReviewAggregateResponse>> getAggregatesByServiceIds(@RequestBody List<Long> serviceIds) {
        Map<Long, ReviewAggregateResponse> aggregates = reviewService.getAggregateByServiceIds(serviceIds);
        return ResponseEntity.ok(aggregates);
    }
}
