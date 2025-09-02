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
@Table(name = "service_items")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @NotNull
    private String serviceName;

    @NotNull
    private String serviceCategory;

    @NotNull
    private String serviceDescription;

    @NotNull
    private Double servicePricePerHour;

    @NotNull
    private Long serviceProviderId;

    @NotNull
    private Double serviceRadius; //In kilometers
}
