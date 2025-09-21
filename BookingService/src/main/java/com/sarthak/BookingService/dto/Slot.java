package com.sarthak.BookingService.dto;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record Slot(
        LocalTime startTime,
        LocalTime endTime
) {}
