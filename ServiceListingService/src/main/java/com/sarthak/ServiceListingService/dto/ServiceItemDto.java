package com.sarthak.ServiceListingService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record ServiceItemDto (
        Long serviceId,

        @NotNull String serviceName,

        @NotNull String serviceDescription,

        @NotNull String serviceCategory,

        @NotNull double servicePricePerHour,

        @NotNull Long serviceProviderId,

        @NotNull Double latitude,

        @NotNull Double longitude,

        ReviewAggregateResponse reviewAggregate
){}
