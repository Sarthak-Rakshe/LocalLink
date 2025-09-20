package com.sarthak.ServiceListingService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_items",
        indexes     = {
                @Index(name = "idx_service_provider", columnList = "service_provider_id"),
                @Index(name = "idx_service_category", columnList = "service_category"),
                @Index(name = "idx_service_location", columnList = "latitude, longitude"),
                @Index(name = "idx_price_per_hour", columnList = "price_per_hour"),
                @Index(name = "idx_service_name_provider", columnList = "service_name, service_provider_id")
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
}
