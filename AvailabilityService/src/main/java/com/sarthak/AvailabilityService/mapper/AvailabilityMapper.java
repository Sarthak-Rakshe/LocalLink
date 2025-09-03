package com.sarthak.AvailabilityService.mapper;

import com.sarthak.AvailabilityService.dto.AvailabilityDto;
import com.sarthak.AvailabilityService.model.Availability;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AvailabilityMapper {

    public AvailabilityDto toDto(Availability availability) {
        AvailabilityDto dto = new AvailabilityDto();
        dto.setAvailabilityId(availability.getAvailabilityId());
        dto.setServiceProviderId(availability.getServiceProviderId());
        dto.setAvailabilityDate(availability.getAvailabilityDate().toString());
        dto.setStartTime(availability.getStartTime().toString());
        dto.setEndTime(availability.getEndTime().toString());
        return dto;
    }


    public Availability toEntity(AvailabilityDto dto) {
        Availability availability = new Availability();
        availability.setAvailabilityId(dto.getAvailabilityId());
        availability.setServiceProviderId(dto.getServiceProviderId());
        availability.setAvailabilityDate(java.time.LocalDate.parse(dto.getAvailabilityDate()));
        availability.setAndNormalizeStartTime(Instant.parse(dto.getStartTime()));
        availability.setAndNormalizeEndTime(Instant.parse(dto.getEndTime()));
        return availability;
    }
}
