package com.sarthak.ServiceListingService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class ServiceItemDto {

    private Long serviceId;

    @NotNull
    private String serviceName;

    @NotNull
    private String serviceDescription;

    @NotNull
    private String serviceCategory;

    @NotNull
    private double servicePricePerHour;

    @NotNull
    private Long serviceProviderId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private ReviewAggregateResponse reviewAggregate;
}
