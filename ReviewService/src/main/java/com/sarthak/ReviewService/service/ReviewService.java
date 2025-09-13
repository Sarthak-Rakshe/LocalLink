package com.sarthak.ReviewService.service;

import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.exception.DuplicateReviewForSameServiceException;
import com.sarthak.ReviewService.exception.EntityNotFoundException;
import com.sarthak.ReviewService.mapper.ReviewMapper;
import com.sarthak.ReviewService.model.Review;
import com.sarthak.ReviewService.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository, ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }

    public ReviewDto addReview(ReviewDto reviewDto){
        if (reviewRepository.findByCustomerIdAndServiceId(reviewDto.customerId(), reviewDto.serviceId()).isPresent()) {
            throw new DuplicateReviewForSameServiceException("Only one review per service allowed");
        }

        Review review = reviewMapper.mapToEntity(reviewDto);

        return reviewMapper.mapToDto(reviewRepository.save(review));
    }

    public Double getAverageRatingForService(Long serviceId){
        return reviewRepository.findAverageRatingByServiceId(serviceId)
                .orElse(0.0);
    }

    public Double calculateAverageRatingForServiceProvider(Long serviceProviderId){
        return reviewRepository.findAverageRatingByServiceProviderId(serviceProviderId)
                .orElse(0.0);
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
}
