package com.sarthak.ServiceListingService.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
public record ServiceItemDto (
        Long serviceId,
        String serviceName,
        String serviceDescription,
        String serviceCategory,
        double servicePricePerHour,
        Long serviceProviderId,
        Double latitude,
        Double longitude
){
}
