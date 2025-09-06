package com.sarthak.BookingService.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityStatusRequest {
    private Long serviceProviderId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
}
