package com.sarthak.AvailabilityService.dto;

import com.sarthak.AvailabilityService.model.ExceptionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ProviderExceptionDto {


    private Long exceptionId;
    private Long serviceProviderId;
    private Long serviceId;
    private String exceptionDate;
    private String newStartTime;
    private String newEndTime;
    private String exceptionReason;
    private ExceptionType exceptionType;
}
