package com.sarthak.ServiceListingService.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceItemDto {

    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private String serviceCategory;
    private double servicePricePerHour;
    private Long serviceProviderId;
    private double serviceRadius; //In kilometers

}
