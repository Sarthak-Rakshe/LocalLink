package com.sarthak.AvailabilityService.mapper;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.model.AvailabilityRules;
import com.sarthak.AvailabilityService.model.ProviderExceptions;
import org.springframework.stereotype.Component;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class AvailabilityMapper {

    public AvailabilityRules AvailabilityDtoToEntity(AvailabilityRulesDto availabilityRulesDto){
        AvailabilityRules rules = new AvailabilityRules();
        rules.setServiceProviderId(availabilityRulesDto.getServiceProviderId());
        rules.setDayOfWeek(DayOfWeek.valueOf(availabilityRulesDto.getDayOfWeek()));
        rules.setStartTime(LocalTime.parse(availabilityRulesDto.getStartTime()));
        rules.setEndTime(LocalTime.parse(availabilityRulesDto.getEndTime()));
        return rules;
    }

    public AvailabilityRulesDto AvailabilityToDto(AvailabilityRules availabilityRules){
        AvailabilityRulesDto dto = new AvailabilityRulesDto();
        dto.setRuleId(availabilityRules.getRuleId());
        dto.setServiceProviderId(availabilityRules.getServiceProviderId());
        dto.setDayOfWeek(availabilityRules.getDayOfWeek().name());
        dto.setStartTime(availabilityRules.getStartTime().toString());
        dto.setEndTime(availabilityRules.getEndTime().toString());
        return dto;
    }

    public ProviderExceptions DtoToProviderException(ProviderExceptionDto dto){
        ProviderExceptions exception = new ProviderExceptions();
        exception.setServiceProviderId(dto.getServiceProviderId());
        exception.setExceptionDate(LocalDate.parse(dto.getExceptionDate()));
        exception.setNewStartTime(LocalTime.parse(dto.getNewStartTime()));
        exception.setNewEndTime(LocalTime.parse(dto.getNewEndTime()));
        exception.setExceptionReason(dto.getExceptionReason());
        exception.setExceptionType(dto.getExceptionType());
        return exception;
    }

    public ProviderExceptionDto ProviderExceptionToDto(ProviderExceptions exception){
        ProviderExceptionDto dto = new ProviderExceptionDto();
        dto.setExceptionId(exception.getExceptionId());
        dto.setServiceProviderId(exception.getServiceProviderId());
        dto.setExceptionDate(exception.getExceptionDate().toString());
        dto.setNewStartTime(exception.getNewStartTime().toString());
        dto.setNewEndTime(exception.getNewEndTime().toString());
        dto.setExceptionReason(exception.getExceptionReason());
        dto.setExceptionType(exception.getExceptionType());
        return dto;
    }



}
