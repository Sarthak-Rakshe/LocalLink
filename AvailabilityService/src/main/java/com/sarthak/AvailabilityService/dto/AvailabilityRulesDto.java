package com.sarthak.AvailabilityService.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@NoArgsConstructor
public class AvailabilityRulesDto {

    private Long ruleId;
    private Long serviceProviderId;
    private Long serviceId;
    private DayOfWeek[] daysOfWeek;
    private String startTime;
    private String endTime;

}
