package com.sarthak.ReviewService.service;

import com.sarthak.ReviewService.dto.ReviewDto;
import com.sarthak.ReviewService.exception.DuplicateReviewForSameServiceException;
import com.sarthak.ReviewService.exception.EntityNotFoundException;
import com.sarthak.ReviewService.mapper.ReviewMapper;
import com.sarthak.ReviewService.model.Review;
import com.sarthak.ReviewService.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    // Use a real mapper since it's a simple, stateless class
    private ReviewMapper reviewMapper;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewMapper = new ReviewMapper();
        reviewService = new ReviewService(reviewRepository, reviewMapper);
    }

    @Test
    void addReview_whenNoDuplicate_savesAndReturnsDto() {
        // Arrange
        ReviewDto input = ReviewDto.builder()
                .serviceProviderId(10L)
                .serviceId(20L)
                .customerId(30L)
                .rating(5)
                .comment("Great job")
                .build();

        when(reviewRepository.findByCustomerIdAndServiceId(30L, 20L)).thenReturn(Optional.empty());

        Review savedEntity = Review.builder()
                .reviewId(1L)
                .serviceProviderId(10L)
                .serviceId(20L)
                .customerId(30L)
                .rating(5)
                .comment("Great job")
                .build();
        when(reviewRepository.save(any(Review.class))).thenReturn(savedEntity);

        // Act
        ReviewDto result = reviewService.addReview(input);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.reviewId()).isEqualTo(1L);
        assertThat(result.serviceProviderId()).isEqualTo(10L);
        assertThat(result.serviceId()).isEqualTo(20L);
        assertThat(result.customerId()).isEqualTo(30L);
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Great job");

        // Verify the entity passed to save is mapped from DTO
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        Review toSave = captor.getValue();
        assertThat(toSave.getReviewId()).isNull(); // new review
        assertThat(toSave.getServiceProviderId()).isEqualTo(10L);
        assertThat(toSave.getServiceId()).isEqualTo(20L);
        assertThat(toSave.getCustomerId()).isEqualTo(30L);
        assertThat(toSave.getRating()).isEqualTo(5);
        assertThat(toSave.getComment()).isEqualTo("Great job");

        verify(reviewRepository).findByCustomerIdAndServiceId(30L, 20L);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    void addReview_whenDuplicate_throwsDuplicateException() {
        // Arrange
        ReviewDto input = ReviewDto.builder()
                .serviceProviderId(10L)
                .serviceId(20L)
                .customerId(30L)
                .rating(4)
                .comment("Nice")
                .build();

        Review existing = Review.builder().reviewId(99L).build();
        when(reviewRepository.findByCustomerIdAndServiceId(30L, 20L)).thenReturn(Optional.of(existing));

        // Act + Assert
        assertThrows(DuplicateReviewForSameServiceException.class, () -> reviewService.addReview(input));

        verify(reviewRepository).findByCustomerIdAndServiceId(30L, 20L);
        verifyNoMoreInteractions(reviewRepository);
    }

    @Test
    void getAverageRatingForService_whenPresent_returnsValue() {
        when(reviewRepository.findAverageRatingByServiceId(20L)).thenReturn(Optional.of(4.5));
        Double avg = reviewService.getAverageRatingForService(20L);
        assertEquals(4.5, avg);
    }

    @Test
    void getAverageRatingForService_whenEmpty_returnsZero() {
        when(reviewRepository.findAverageRatingByServiceId(21L)).thenReturn(Optional.empty());
        Double avg = reviewService.getAverageRatingForService(21L);
        assertEquals(0.0, avg);
    }

    @Test
    void calculateAverageRatingForServiceProvider_whenPresent_returnsValue() {
        when(reviewRepository.findAverageRatingByServiceProviderId(10L)).thenReturn(Optional.of(3.2));
        Double avg = reviewService.calculateAverageRatingForServiceProvider(10L);
        assertEquals(3.2, avg);
    }

    @Test
    void calculateAverageRatingForServiceProvider_whenEmpty_returnsZero() {
        when(reviewRepository.findAverageRatingByServiceProviderId(11L)).thenReturn(Optional.empty());
        Double avg = reviewService.calculateAverageRatingForServiceProvider(11L);
        assertEquals(0.0, avg);
    }

    @Test
    void getById_whenFound_returnsDto() {
        Review entity = Review.builder()
                .reviewId(1L)
                .serviceProviderId(10L)
                .serviceId(20L)
                .customerId(30L)
                .rating(5)
                .comment("Great job")
                .build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(entity));

        ReviewDto dto = reviewService.getById(1L);
        assertThat(dto).isNotNull();
        assertThat(dto.reviewId()).isEqualTo(1L);
        assertThat(dto.comment()).isEqualTo("Great job");
    }

    @Test
    void getById_whenMissing_throwsEntityNotFound() {
        when(reviewRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> reviewService.getById(404L));
    }

    @Test
    void getReviewsForService_mapsEntitiesToDtos_withPagination() {
        PageRequest pr = PageRequest.of(0, 2);
        List<Review> reviews = List.of(
                Review.builder().reviewId(1L).serviceProviderId(10L).serviceId(20L).customerId(30L).rating(4).comment("Good").build(),
                Review.builder().reviewId(2L).serviceProviderId(11L).serviceId(20L).customerId(31L).rating(5).comment("Great").build()
        );
        Page<Review> page = new PageImpl<>(reviews, pr, 2);
        when(reviewRepository.findAllByServiceId(eq(20L), any(PageRequest.class))).thenReturn(page);

        Page<ReviewDto> dtoPage = reviewService.getReviewsForService(20L, 0, 2);
        assertThat(dtoPage.getTotalElements()).isEqualTo(2);
        assertThat(dtoPage.getContent()).extracting(ReviewDto::reviewId).containsExactly(1L, 2L);
        assertThat(dtoPage.getContent()).extracting(ReviewDto::rating).containsExactly(4, 5);
    }

    @Test
    void getReviewsForCustomer_mapsEntitiesToDtos_withPagination() {
        PageRequest pr = PageRequest.of(1, 3);
        List<Review> reviews = List.of(
                Review.builder().reviewId(3L).serviceProviderId(10L).serviceId(22L).customerId(55L).rating(2).comment("Bad").build(),
                Review.builder().reviewId(4L).serviceProviderId(12L).serviceId(22L).customerId(55L).rating(3).comment("Okay").build(),
                Review.builder().reviewId(5L).serviceProviderId(13L).serviceId(22L).customerId(55L).rating(5).comment("Awesome").build()
        );
        Page<Review> page = new PageImpl<>(reviews, pr, 3);
        when(reviewRepository.findAllByCustomerId(eq(55L), any(PageRequest.class))).thenReturn(page);

        Page<ReviewDto> dtoPage = reviewService.getReviewsForCustomer(55L, 1, 3);
        assertThat(dtoPage.getContent()).hasSize(3);
        assertThat(dtoPage.getContent()).extracting(ReviewDto::reviewId).containsExactly(3L, 4L, 5L);
        assertThat(dtoPage.getContent()).extracting(ReviewDto::comment).containsExactly("Bad", "Okay", "Awesome");
    }
}
