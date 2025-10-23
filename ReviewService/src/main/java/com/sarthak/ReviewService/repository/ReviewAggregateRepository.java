package com.sarthak.ReviewService.repository;

import com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse;
import com.sarthak.ReviewService.model.ReviewAggregate;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewAggregateRepository extends JpaRepository<ReviewAggregate,Long > {

    @Query("""
        SELECT new com.sarthak.ReviewService.dto.response.ProviderReviewAggregateResponse(
            r.serviceProviderId,
            SUM(r.averageRating * r.totalReviews) / SUM(r.totalReviews),
            SUM(r.totalReviews)
        )
        FROM ReviewAggregate r
        WHERE r.serviceProviderId IN (:serviceProviderIds)
        GROUP BY r.serviceProviderId
    """)
    List<ProviderReviewAggregateResponse> aggregateServiceProviderReviews(@Param("serviceProviderIds") List<Long> serviceProviderIds);

    Optional<ReviewAggregate> findByServiceProviderIdAndServiceId(@NotNull Long serviceProviderId, @NotNull Long serviceId);

    Optional<Double> findByServiceId(Long serviceId);

    @Query("SELECT r FROM ReviewAggregate r WHERE r.serviceId IN :serviceIds")
    List<ReviewAggregate> findAllByServiceIds(List<Long> serviceIds);
}
