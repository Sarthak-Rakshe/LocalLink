package com.sarthak.AvailabilityService.dto.request;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AvailabilitySlotsRequest (
        LocalDate date,
        Long serviceProviderId,
        Long serviceId
){}
