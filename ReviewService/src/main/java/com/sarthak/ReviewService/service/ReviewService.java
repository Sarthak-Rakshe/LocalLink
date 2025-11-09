package com.sarthak.ReviewService.service;

import com.sarthak.ReviewService.client.BookingClient;
import com.sarthak.ReviewService.dto.BookingDto;
import com.sarthak.ReviewService.dto.ReviewAggregateResponse;
import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
import com.sarthak.ReviewService.exception.BookingNotCompleteException;
import com.sarthak.ReviewService.exception.DuplicateReviewForSameServiceException;
import com.sarthak.ReviewService.exception.EntityNotFoundException;
import com.sarthak.ReviewService.mapper.ReviewMapper;
import com.sarthak.ReviewService.model.Review;
import com.sarthak.ReviewService.model.ReviewAggregate;
import com.sarthak.ReviewService.repository.ReviewAggregateRepository;
import com.sarthak.ReviewService.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewAggregateRepository reviewAggregateRepository;
    private final ReviewMapper reviewMapper;
    private final BookingClient bookingClient;
    private final List<String> ALLOWED_SORT_FIELDS = List.of("reviewId", "createdAt", "updatedAt", "rating");

    public ReviewService(ReviewRepository reviewRepository, ReviewMapper reviewMapper, ReviewAggregateRepository reviewAggregateRepository, BookingClient bookingClient) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.reviewAggregateRepository = reviewAggregateRepository;
        this.bookingClient = bookingClient;
    }

    public ReviewDto validateAndAddReview(ReviewDto reviewDto){
        log.info("Validating if user is authorized to add review: {}", reviewDto);
        BookingDto bookingDto = bookingClient.getBookingDetails(reviewDto.bookingId());
        if (bookingDto == null) {
            throw new EntityNotFoundException("Booking not found for ID: " + reviewDto.bookingId());
        }
        if (!bookingDto.bookingStatus().equalsIgnoreCase("COMPLETED")){
            throw new BookingNotCompleteException("Booking status should be complete to add review");
        }
        return addReview(reviewDto);
    }

    @Transactional
    private ReviewDto addReview(ReviewDto reviewDto){
        log.info("Adding review: {}", reviewDto);
        if (reviewRepository.findByCustomerIdAndServiceId(reviewDto.customerId(), reviewDto.serviceId()).isPresent()) {
            log.error("Duplicate review attempt by customer {} for service {}", reviewDto.customerId(), reviewDto.serviceId());
            throw new DuplicateReviewForSameServiceException("Only one review per service allowed");
        }

        Review review = reviewMapper.mapToEntity(reviewDto);
        log.info("Saving review: {}", review);
        Review savedReview = reviewRepository.save(review);
        ReviewAggregate aggregate =
                reviewAggregateRepository.findByServiceProviderIdAndServiceId(savedReview.getServiceProviderId(),
                                savedReview.getServiceId())
                .orElse(
                        ReviewAggregate.builder()
                                .serviceProviderId(savedReview.getServiceProviderId())
                                .serviceId(savedReview.getServiceId())
                                .averageRating(0.0)
                                .totalReviews(0L)
                                .build()
                );
        log.info("Current aggregate before adding review: {}", aggregate);
        aggregate.addReview(savedReview.getRating());
        reviewAggregateRepository.save(aggregate);
        log.info("Updated aggregate after adding review: {}", aggregate);
        return reviewMapper.mapToDto(savedReview);
    }

    public Page<ReviewDto> getReviewsByServiceProviderId(Long userId, int page, int size, String sortBy, String sortDir){
        Pageable pageable = getPageable(page, size, sortBy, sortDir);
        Page<Review> reviews = reviewRepository.findAllByServiceProviderId(userId, pageable);
        return reviews.map(reviewMapper::mapToDto);
    }

    public Page<ReviewDto> getReviewsForService(Long serviceId, int page, int size, String sortBy, String sortDir){
        Pageable pageable = getPageable(page, size, sortBy, sortDir);

        log.info("Fetching reviews for serviceId: {} with page: {} and size: {}", serviceId, page, size);

        return reviewRepository.findAllByServiceId(serviceId, pageable)
                .map(r -> ReviewDto.builder()
                        .reviewId(r.getReviewId())
                        .serviceProviderId(r.getServiceProviderId())
                        .serviceId(r.getServiceId())
                        .bookingId(r.getBookingId())
                        .customerId(r.getCustomerId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .build()
                );
    }

    public Page<ReviewDto> getReviewsForCustomer(Long customerId, int page, int size, String sortBy, String sortDir){
        Pageable pageable = getPageable(page, size, sortBy, sortDir);

        log.info("Fetching reviews for customerId: {} with page: {} and size: {}", customerId, page, size);
        return reviewRepository.findAllByCustomerId(customerId, pageable)
                .map(r -> ReviewDto.builder()
                        .reviewId(r.getReviewId())
                        .serviceProviderId(r.getServiceProviderId())
                        .serviceId(r.getServiceId())
                        .bookingId(r.getBookingId())
                        .customerId(r.getCustomerId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .build()
                );
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto, Long userId){
        log.info("Updating review with id: {} using data: {}", reviewId, reviewDto);
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));
        if(!Objects.equals(existing.getCustomerId(), userId)){
            log.error("Unauthorized update attempt by user {} on review {}", userId, reviewId);
            throw new AccessDeniedException("You are not allowed to update this review");
        }

        log.info("Existing review found: {}", existing);
        ReviewAggregate aggregate = reviewAggregateRepository.findByServiceProviderIdAndServiceId(existing.getServiceProviderId(), existing.getServiceId())
                .orElseThrow(()-> new EntityNotFoundException("Review aggregate not found"));

        log.info("Current aggregate before updating review: {}", aggregate);
        aggregate.updateReview(existing.getRating(), reviewDto.rating());
        log.info("Updated aggregate after updating review: {}", aggregate);

        existing.setRating(reviewDto.rating());
        log.info("Updated review rating from {} to {}", existing.getRating(), reviewDto.rating());
        existing.setComment(reviewDto.comment());

        log.info("Saving updated aggregate: {}", aggregate);
        reviewAggregateRepository.save(aggregate);
        log.info("Saving updated review: {}", existing);

        return reviewMapper.mapToDto(reviewRepository.save(existing));
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId){
        log.info("Deleting review with id: {}", reviewId);
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));

        if (!Objects.equals(existing.getCustomerId(), userId)) {
            log.error("Unauthorized delete attempt by user {} on review {}", userId, reviewId);
            throw new AccessDeniedException("You are not allowed to delete this review");
        }

        ReviewAggregate aggregate = reviewAggregateRepository.findByServiceProviderIdAndServiceId(existing.getServiceProviderId(), existing.getServiceId())
                .orElseThrow(()-> new EntityNotFoundException("Review aggregate not found"));
        log.info("Current aggregate before deleting review: {}", aggregate);

        aggregate.deleteReview(existing.getRating());
        if(aggregate.getTotalReviews() == 0){
            log.info("No more reviews left for serviceId: {} and serviceProviderId: {}. Deleting aggregate.",
                    aggregate.getServiceId(), aggregate.getServiceProviderId());
            reviewAggregateRepository.delete(aggregate);
        }else{
            log.info("Updated aggregate after deleting review: {}", aggregate);
            reviewAggregateRepository.save(aggregate);
        }

        log.info("Deleting review: {}", existing);
        reviewRepository.deleteById(reviewId);
    }

    public Double getAverageRatingForService(Long serviceId){
        log.info("Fetching average rating for serviceId: {}", serviceId);
        return reviewAggregateRepository.findByServiceId(serviceId)
                .orElse(0.0);
    }


    public Map<Long, ProviderReviewAggregateResponse> getReviewAggregateForProvider(List<Long> serviceProviderIds) {
        log.info("Fetching review aggregate for serviceProviderId: {}", serviceProviderIds);
        List<ProviderReviewAggregateResponse> aggregates = reviewAggregateRepository.aggregateServiceProviderReviews(serviceProviderIds);
        Map<Long, ProviderReviewAggregateResponse> responseMap = new HashMap<>();
        if(aggregates.isEmpty()){
            return responseMap;
        }
        for(ProviderReviewAggregateResponse aggregate : aggregates){
            responseMap.put(aggregate.serviceProviderId(), aggregate);
        }
        return responseMap;
    }

    private Pageable getPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10; // default page size
        if(sortBy == null || sortBy.isEmpty()) sortBy = "createdAt";
        String sortDirNormalized = (sortDir != null) ? sortDir.toLowerCase() : "desc";

        if (!sortDirNormalized.equals("asc") && !sortDirNormalized.equals("desc")) {
            sortDirNormalized = "desc"; // default to descending if invalid
        }

        String sortField = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";

        Sort sort = sortDirNormalized.equals("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        return PageRequest.of(page, size, sort);
    }

    public List<ReviewDto> getByServiceIds(List<Long> serviceIds) {
        log.info("Fetching reviews for ids: {}", serviceIds);
        List<Review> reviews = reviewRepository.findAllByServiceIds(serviceIds);
        return reviews.stream()
                .map(reviewMapper::mapToDto)
                .toList();
    }

    public Map<Long, ReviewAggregateResponse> getAggregateByServiceIds(List<Long> serviceIds){
        log.info("Fetching review aggregates for service ids: {}", serviceIds);
        List<ReviewAggregate> aggregates = reviewAggregateRepository.findAllByServiceIds(serviceIds);
        Map<Long, ReviewAggregateResponse> responseMap = new HashMap<>();
        for(ReviewAggregate aggregate : aggregates){
            responseMap.put(aggregate.getServiceId(), reviewMapper.mapToAggregateResponse(aggregate));
        }
        return responseMap;
    }
}
