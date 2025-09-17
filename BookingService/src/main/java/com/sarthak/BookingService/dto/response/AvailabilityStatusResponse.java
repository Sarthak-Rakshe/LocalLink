package com.sarthak.BookingService.dto.response;

import com.sarthak.BookingService.dto.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record AvailabilityStatusResponse(
        Long serviceProviderId,
        String startTime,
        String endTime,
        String date,
        AvailabilityStatus status
) {}

