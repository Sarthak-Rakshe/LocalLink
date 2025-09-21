package com.sarthak.AvailabilityService.dto;

import java.util.List;

public record BookedSlotsResponse(
        Long serviceProviderId,
        Long serviceId,
        List<Slot> bookedSlots,
        String date
) {}
