package com.sarthak.ReviewService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "review_aggregates", indexes = {
        @Index(name = "idx_service_provider_service", columnList = "service_provider_id, service_id"),
        @Index(name = "idx_service", columnList = "service_id")
})
public class ReviewAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aggregateId;

    @NotNull
    @Column(name = "service_provider_id", nullable = false)
    private Long serviceProviderId;

    @NotNull
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @NotNull
    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    @NotNull
    @Column(name = "total_reviews", nullable = false)
    private Long totalReviews;

    public void addReview(int rating) {
        this.totalReviews += 1;
        this.averageRating = ((this.averageRating * (this.totalReviews - 1)) + rating) / this.totalReviews;
    }

    public void deleteReview(int rating) {
        if (this.totalReviews > 1) {
            this.averageRating = ((this.averageRating * this.totalReviews) - rating) / (this.totalReviews - 1);
            this.totalReviews -= 1;
        } else {
            this.averageRating = 0.0;
            this.totalReviews = 0L;
        }
    }

    public void updateReview(int oldRating, int newRating) {
        this.averageRating = ((this.averageRating * this.totalReviews) - oldRating + newRating) / this.totalReviews;
    }

}
