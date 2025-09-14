package com.sarthak.ReviewService.repository;

import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
import com.sarthak.ReviewService.model.ReviewAggregate;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewAggregateRepository extends JpaRepository<ReviewAggregate,Long > {

    @Query("""
            SELECT SUM(r.averageRating * r.totalReviews) / SUM(r.totalReviews) AS averageRating,
                   SUM(r.totalReviews) AS totalReviews
            FROM ReviewAggregate r
            WHERE r.serviceProviderId = :serviceProviderId
            """
    )
    Optional<ProviderReviewAggregateResponse> aggregateServiceProviderReviews(Long serviceProviderId);

    Optional<ReviewAggregate> findByServiceProviderIdAndServiceId(@NotNull Long serviceProviderId, @NotNull Long serviceId);

    Optional<Double> findByServiceId(Long serviceId);
}
