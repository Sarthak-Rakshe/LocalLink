package com.sarthak.ReviewService.service;

import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewAggregateRepository reviewAggregateRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository, ReviewMapper reviewMapper, ReviewAggregateRepository reviewAggregateRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.reviewAggregateRepository = reviewAggregateRepository;
    }

    @Transactional
    public ReviewDto addReview(ReviewDto reviewDto){
        log.info("Adding review: {}", reviewDto);
        if (reviewRepository.findByCustomerIdAndServiceId(reviewDto.customerId(), reviewDto.serviceId()).isPresent()) {
            log.error("Duplicate review attempt by customer {} for service {}", reviewDto.customerId(), reviewDto.serviceId());
            throw new DuplicateReviewForSameServiceException("Only one review per service allowed");
        }

        Review review = reviewMapper.mapToEntity(reviewDto);
        ReviewAggregate aggregate = reviewAggregateRepository.findByServiceProviderIdAndServiceId(review.getServiceProviderId(),review.getServiceId())
                .orElse(
                        ReviewAggregate.builder()
                                .serviceProviderId(review.getServiceProviderId())
                                .serviceId(review.getServiceId())
                                .averageRating(0.0)
                                .totalReviews(0L)
                                .build()
                );
        log.info("Current aggregate before adding review: {}", aggregate);
        aggregate.addReview(review.getRating());
        reviewAggregateRepository.save(aggregate);
        log.info("Updated aggregate after adding review: {}", aggregate);
        log.info("Saving review: {}", review);
        return reviewMapper.mapToDto(reviewRepository.save(review));
    }

    public ReviewDto getById(Long reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));
        return reviewMapper.mapToDto(review);
    }

    public Page<ReviewDto> getReviewsForService(Long serviceId, int page, int size){
        PageRequest pr = PageRequest.of(page, size);

        log.info("Fetching reviews for serviceId: {} with page: {} and size: {}", serviceId, page, size);

        return reviewRepository.findAllByServiceId(serviceId, pr)
                .map(r -> ReviewDto.builder()
                        .reviewId(r.getReviewId())
                        .serviceProviderId(r.getServiceProviderId())
                        .serviceId(r.getServiceId())
                        .customerId(r.getCustomerId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .build()
                );
    }

    public Page<ReviewDto> getReviewsForCustomer(Long customerId, int page, int size){
        Pageable pg = PageRequest.of(page, size);

        log.info("Fetching reviews for customerId: {} with page: {} and size: {}", customerId, page, size);
        return reviewRepository.findAllByCustomerId(customerId, pg)
                .map(r -> ReviewDto.builder()
                        .reviewId(r.getReviewId())
                        .serviceProviderId(r.getServiceProviderId())
                        .serviceId(r.getServiceId())
                        .customerId(r.getCustomerId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .build()
                );
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto){
        log.info("Updating review with id: {} using data: {}", reviewId, reviewDto);
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));

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
    public void deleteReview(Long reviewId){
        log.info("Deleting review with id: {}", reviewId);
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));

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


    public ProviderReviewAggregateResponse getReviewAggregateForProvider(Long serviceProviderId) {
        log.info("Fetching review aggregate for serviceProviderId: {}", serviceProviderId);
        return reviewAggregateRepository.aggregateServiceProviderReviews(serviceProviderId)
                .orElse(ProviderReviewAggregateResponse.builder()
                        .averageRating(0.0)
                        .totalReviews(0L)
                        .build());
    }
}
