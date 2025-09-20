package com.sarthak.AvailabilityService.dto.response;

import com.sarthak.AvailabilityService.dto.Slot;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AvailabilitySlotsResponse (
        LocalDate date,
        List<Slot> availableSlots,
        boolean isDayAvailable
){
}
