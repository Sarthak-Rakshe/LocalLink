package com.sarthak.BookingService.dto;

import com.sarthak.BookingService.model.BookingStatus;
import lombok.Builder;

@Builder
public record BookingStatusCount(
        BookingStatus status,
        Long count
) {}
