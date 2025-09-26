package com.sarthak.ServiceListingService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "service_items",
        indexes     = {
                @Index(name = "idx_service_provider", columnList = "service_provider_id"),
                @Index(name = "idx_service_category", columnList = "service_category"),
                @Index(name = "idx_service_location", columnList = "latitude, longitude"),
                @Index(name = "idx_price_per_hour", columnList = "service_price_per_hour"),
                @Index(name = "idx_service_name_provider", columnList = "service_name, service_provider_id"),
                @Index(name = "idx_service_category_provider", columnList = "service_category, service_provider_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_updated_at", columnList = "updated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_service_provider_service",
                        columnNames = {"service_provider_id", "service_name"})
        }
)
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @NotNull
    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @NotNull
    @Column(name = "service_category", nullable = false)
    private String serviceCategory;

    @NotNull
    @Column(name = "service_description", nullable = false)
    private String serviceDescription;

    @NotNull
    @Column(name = "service_price_per_hour", nullable = false)
    private Double servicePricePerHour;

    @NotNull
    @Column(name = "service_provider_id", nullable = false, unique = true)
    private Long serviceProviderId;

    @NotNull
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected  void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
