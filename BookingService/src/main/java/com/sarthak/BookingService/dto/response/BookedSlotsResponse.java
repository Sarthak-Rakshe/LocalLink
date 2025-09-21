package com.sarthak.BookingService.dto.response;

import com.sarthak.BookingService.dto.Slot;
import lombok.Builder;

import java.util.List;

@Builder
public record BookedSlotsResponse(
        Long serviceProviderId,
        Long serviceId,
        List<Slot> bookedSlots,
        String date
) {}
