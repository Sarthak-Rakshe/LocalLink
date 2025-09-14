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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (reviewRepository.findByCustomerIdAndServiceId(reviewDto.customerId(), reviewDto.serviceId()).isPresent()) {
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
        aggregate.addReview(review.getRating());
        reviewAggregateRepository.save(aggregate);

        return reviewMapper.mapToDto(reviewRepository.save(review));
    }

    public ReviewDto getById(Long reviewId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));
        return reviewMapper.mapToDto(review);
    }

    public Page<ReviewDto> getReviewsForService(Long serviceId, int page, int size){
        PageRequest pr = PageRequest.of(page, size);

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
        PageRequest pr = PageRequest.of(page, size);

        return reviewRepository.findAllByCustomerId(customerId, pr)
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
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));

        ReviewAggregate aggregate = reviewAggregateRepository.findByServiceProviderIdAndServiceId(existing.getServiceProviderId(), existing.getServiceId())
                .orElseThrow(()-> new EntityNotFoundException("Review aggregate not found"));

        aggregate.updateReview(existing.getRating(), reviewDto.rating());

        existing.setRating(reviewDto.rating());
        existing.setComment(reviewDto.comment());

        return reviewMapper.mapToDto(reviewRepository.save(existing));
    }

    @Transactional
    public void deleteReview(Long reviewId){
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new EntityNotFoundException("Review not found"));

        ReviewAggregate aggregate = reviewAggregateRepository.findByServiceProviderIdAndServiceId(existing.getServiceProviderId(), existing.getServiceId())
                .orElseThrow(()-> new EntityNotFoundException("Review aggregate not found"));

        aggregate.deleteReview(existing.getRating());
        if(aggregate.getTotalReviews() == 0){
            reviewAggregateRepository.delete(aggregate);
        }else{
            reviewAggregateRepository.save(aggregate);
        }

        reviewRepository.deleteById(reviewId);
    }

    public Double getAverageRatingForService(Long serviceId){
        return reviewAggregateRepository.findByServiceId(serviceId)
                .orElse(0.0);
    }


    public ProviderReviewAggregateResponse getReviewAggregateForProvider(Long serviceProviderId) {
        return reviewAggregateRepository.aggregateServiceProviderReviews(serviceProviderId)
                .orElse(ProviderReviewAggregateResponse.builder()
                        .averageRating(0.0)
                        .totalReviews(0L)
                        .build());
    }
}
