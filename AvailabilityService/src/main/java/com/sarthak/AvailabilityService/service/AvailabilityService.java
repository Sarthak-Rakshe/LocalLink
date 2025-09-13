package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.mapper.AvailabilityMapper;
import com.sarthak.AvailabilityService.model.AvailabilityRules;
import com.sarthak.AvailabilityService.model.ExceptionType;
import com.sarthak.AvailabilityService.model.ProviderExceptions;
import com.sarthak.AvailabilityService.repository.AvailabilityRulesRepository;
import com.sarthak.AvailabilityService.repository.ProviderExceptionsRepository;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilityStatusResponse;
import com.sarthak.AvailabilityService.dto.response.Status;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AvailabilityService{

    private final AvailabilityRulesRepository availabilityRulesRepository;
    private final ProviderExceptionsRepository providerExceptionsRepository;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityService(AvailabilityRulesRepository availabilityRulesRepository, ProviderExceptionsRepository providerExceptionsRepository, AvailabilityMapper availabilityMapper) {
        this.availabilityRulesRepository = availabilityRulesRepository;
        this.providerExceptionsRepository = providerExceptionsRepository;
        this.availabilityMapper = availabilityMapper;
    }


    public AvailabilityStatusResponse checkAvailability(AvailabilityStatusRequest request){
        Long serviceProviderId = request.getServiceProviderId();
        DayOfWeek day = request.getDate().getDayOfWeek();
        Status status = Status.OUTSIDE_WORKING_HOURS;
        AvailabilityStatusResponse response = new AvailabilityStatusResponse(
                serviceProviderId,
                request.getStartTime().toString(),
                request.getEndTime().toString(),
                request.getDate().toString(),
                status.name()
        );

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findByServiceProviderIdAndDayOfWeek(serviceProviderId,day);
        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findByServiceProviderIdAndExceptionDate(serviceProviderId,request.getDate());

        ExceptionResult isInException = isTimeInAnyException(request, exceptions);
        if(isInException == ExceptionResult.IS_AVAILABLE) {
            response.setStatus(Status.AVAILABLE.name());
            return response;
        }else if (isInException == ExceptionResult.IS_BLOCKED) {
            response.setStatus(Status.BLOCKED.name());
            return response;
        }

        RuleResult isInRules = checkInRules(request, rules);
        if(isInRules == RuleResult.IS_AVAILABLE) {
            response.setStatus(Status.AVAILABLE.name());
            return response;
        }

        return response;

    }

    private RuleResult checkInRules(AvailabilityStatusRequest request, List<AvailabilityRules> rules) {
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        for (AvailabilityRules rule : rules) {
            Boolean isStartTimeWithinRange = isWithinTimeRange(startTime, rule.getStartTime(), rule.getEndTime());
            Boolean isEndTimeWithinRange = isWithinTimeRange(endTime, rule.getStartTime(), rule.getEndTime());
            if (isStartTimeWithinRange && isEndTimeWithinRange) {
                return RuleResult.IS_AVAILABLE;
            }
        }
        return RuleResult.NOT_IN_RULES;
    }

    private ExceptionResult isTimeInAnyException(AvailabilityStatusRequest request, List<ProviderExceptions> exceptions) {
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        LocalDate date = request.getDate();
        for (ProviderExceptions exception : exceptions) {
            if(exception.getExceptionDate() == null || exception.getNewStartTime() == null || exception.getNewEndTime() == null || !exception.getExceptionDate().equals(date)) {
                continue; // Skip this exception if any of the required fields are null
            }
            Boolean isStartTimeWithinRange = isWithinTimeRange(startTime, exception.getNewStartTime(), exception.getNewEndTime());
            Boolean isEndTimeWithinRange = isWithinTimeRange(endTime, exception.getNewStartTime(), exception.getNewEndTime());
            if(isStartTimeWithinRange && isEndTimeWithinRange) {
                if(exception.getExceptionType() == ExceptionType.OVERRIDE) {
                    return ExceptionResult.IS_AVAILABLE;
                } else if (exception.getExceptionType() == ExceptionType.BLOCKED) {
                    return ExceptionResult.IS_BLOCKED;
                }
            }
        }
        return ExceptionResult.NOT_IN_EXCEPTION;
    }

    private boolean isWithinTimeRange(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && !time.isAfter(end);
    }

    public AvailabilityRulesDto addAvailabilityRule(AvailabilityRulesDto availabilityRulesDto){
        AvailabilityRules rules = availabilityMapper.AvailabilityDtoToEntity(availabilityRulesDto);
        AvailabilityRules savedRules = availabilityRulesRepository.save(rules);
        return availabilityMapper.AvailabilityToDto(savedRules);
    }

    public ProviderExceptionDto addProviderException(ProviderExceptionDto providerExceptionDto){
        ProviderExceptions exception = availabilityMapper.DtoToProviderException(providerExceptionDto);
        ProviderExceptions savedException = providerExceptionsRepository.save(exception);
        return availabilityMapper.ProviderExceptionToDto(savedException);
    }
    private enum ExceptionResult {
        IS_AVAILABLE,
        IS_BLOCKED,
        NOT_IN_EXCEPTION
    }

    private enum RuleResult {
        IS_AVAILABLE,
        NOT_IN_RULES
    }

}
