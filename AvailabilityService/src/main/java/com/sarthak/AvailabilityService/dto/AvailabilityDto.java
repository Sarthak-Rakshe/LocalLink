package com.sarthak.AvailabilityService.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AvailabilityDto {

    private Long availabilityId;
    private Long serviceProviderId;
    private String availabilityDate;
    private String startTime;
    private String endTime;
}
