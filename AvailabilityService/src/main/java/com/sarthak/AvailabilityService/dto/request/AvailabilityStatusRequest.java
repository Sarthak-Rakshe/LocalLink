package com.sarthak.AvailabilityService.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AvailabilityStatusRequest {
    private Long serviceProviderId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
}
