package com.sarthak.ReviewService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews", indexes = {
        @Index(name = "idx_service_provider_service_review", columnList = "service_provider_id, service_id"),
        @Index(name = "idx_customer", columnList = "customer_id"),
        @Index(name = "idx_service_provider_rating", columnList="service_provider_id, rating")
        },
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_review", columnNames = {"service_provider_id", "service_id", "customer_id"})
        }
)
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @NotNull
    @Column(name = "service_provider_id", nullable = false)
    private Long serviceProviderId;

    @NotNull
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @NotNull
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull
    @Column(name = "rating", nullable = false)
    @Range(min = 1, max = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotNull
    @NotBlank
    @Column(name = "comment", nullable = false, length = 2048)
    @Size(max = 2048, message = "Comment can be at most 2048 characters long")
    private String comment;


    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    public void onUpdate() {
        updatedAt = Instant.now();
    }

}
