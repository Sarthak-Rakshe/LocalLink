package com.sarthak.AvailabilityService.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AvailabilityRulesDto {

    private Long ruleId;
    private Long serviceProviderId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;

}
