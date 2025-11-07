package com.sarthak.BookingService.dto;

import com.sarthak.BookingService.dto.response.ReviewAggregateResponse;
import lombok.Builder;


@Builder
public record ServiceItemDto(
        Long serviceId,

        String serviceName,

        String serviceDescription,

        String serviceCategory,

        double servicePricePerHour,

        Long serviceProviderId,

        Double latitude,

        Double longitude,

        ReviewAggregateResponse reviewAggregate
){}
