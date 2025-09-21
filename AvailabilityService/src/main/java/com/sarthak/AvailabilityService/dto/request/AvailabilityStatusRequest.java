package com.sarthak.AvailabilityService.dto.request;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record AvailabilityStatusRequest (
        Long serviceProviderId,
        Long serviceId,
        LocalTime startTime,
        LocalTime endTime,
        LocalDate date
){}
