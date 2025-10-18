package com.sarthak.ServiceListingService.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
public record ServiceItemDto (
        Long serviceId,

        @NotNull String serviceName,

        @NotNull String serviceDescription,

        @NotNull String serviceCategory,

        @NotNull double servicePricePerHour,

        @NotNull Long serviceProviderId,

        @NotNull Double latitude,

        @NotNull Double longitude
){
}
