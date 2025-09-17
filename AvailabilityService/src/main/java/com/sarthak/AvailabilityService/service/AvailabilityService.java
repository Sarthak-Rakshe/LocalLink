package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.dto.request.DayAndTimeAvailabilityRequest;
import com.sarthak.AvailabilityService.exception.DuplicateEntityException;
import com.sarthak.AvailabilityService.exception.EntityNotFoundException;
import com.sarthak.AvailabilityService.exception.InvalidTimeSlotParametersException;
import com.sarthak.AvailabilityService.mapper.AvailabilityMapper;
import com.sarthak.AvailabilityService.model.AvailabilityRules;
import com.sarthak.AvailabilityService.model.ExceptionType;
import com.sarthak.AvailabilityService.model.ProviderExceptions;
import com.sarthak.AvailabilityService.repository.AvailabilityRulesRepository;
import com.sarthak.AvailabilityService.repository.ProviderExceptionsRepository;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilityStatusResponse;
import com.sarthak.AvailabilityService.dto.response.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilityService{

    private final AvailabilityRulesRepository availabilityRulesRepository;
    private final ProviderExceptionsRepository providerExceptionsRepository;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityService(AvailabilityRulesRepository availabilityRulesRepository,
                               ProviderExceptionsRepository providerExceptionsRepository,
                               AvailabilityMapper availabilityMapper) {
        this.availabilityRulesRepository = availabilityRulesRepository;
        this.providerExceptionsRepository = providerExceptionsRepository;
        this.availabilityMapper = availabilityMapper;
    }


    public AvailabilityRulesDto createAvailabilityRule(AvailabilityRulesDto dto) {
        AvailabilityRules rule = availabilityMapper.AvailabilityDtoToEntity(dto);

        Optional<AvailabilityRules> existingRule = availabilityRulesRepository
                .findByServiceProviderIdAndServiceIdAndStartTimeAndEndTime(rule.getServiceProviderId(),
                        rule.getServiceId(), rule.getStartTime(), rule.getEndTime());
        if (existingRule.isPresent()) throw new DuplicateEntityException("An availability rule with the same parameters already exists.");

        AvailabilityRules savedRule = availabilityRulesRepository.save(rule);
        return availabilityMapper.AvailabilityToDto(savedRule);
    }

    public ProviderExceptionDto createProviderException(ProviderExceptionDto dto){
        ProviderExceptions exception = availabilityMapper.DtoToProviderException(dto);

        Optional<ProviderExceptions> existingException = providerExceptionsRepository
                .findAllByServiceProviderIdAndServiceIdAndExceptionDateAndNewStartTimeAndNewEndTime(exception.getServiceProviderId(),
                        exception.getServiceId(), exception.getExceptionDate(), exception.getNewStartTime(),
                        exception.getNewEndTime());
        if (existingException.isPresent()) throw new DuplicateEntityException("An exception for the same date " +
                "and time already exists.");

        ProviderExceptions savedException = providerExceptionsRepository.save(exception);
        return availabilityMapper.ProviderExceptionToDto(savedException);
    }


    public Page<AvailabilityRulesDto> getAllAvailabilityRulesOnDayAndTime(DayAndTimeAvailabilityRequest request,
                                                              int page, int size){
        PageRequest pr = PageRequest.of(page, size);
        DayOfWeek dayOfWeek = request.day();
        LocalTime startTime = request.startTime().truncatedTo(ChronoUnit.SECONDS);
        LocalTime endTime = request.endTime().truncatedTo(ChronoUnit.SECONDS);

        Page<AvailabilityRules> rules =  availabilityRulesRepository.findAvailableOnDayAndTime(dayOfWeek.getValue(),
                startTime, endTime, pr);

        return rules.map(availabilityMapper::AvailabilityToDto);
    }

    public List<AvailabilityRulesDto> getAllAvailabilityRulesForProvider(Long serviceProviderId){
        if (serviceProviderId == null){
            throw new IllegalArgumentException("Service Provider ID cannot be null");
        }

        List<AvailabilityRules> rules =  availabilityRulesRepository.findAllByServiceProviderId(serviceProviderId);

        return availabilityMapper.toAvailabilityDtoList(rules);
    }

    public List<AvailabilityRulesDto> getAllAvailabilityRulesForProviderAndService(Long serviceProviderId,
                                                                                 Long serviceId){
        if (serviceProviderId == null || serviceId == null){
            throw new IllegalArgumentException("Service Provider ID and Service ID cannot be null");
        }

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findAllByServiceProviderIdAndServiceId(serviceProviderId, serviceId);

        return availabilityMapper.toAvailabilityDtoList(rules);
    }

    public List<ProviderExceptionDto> getAllExceptionsForProvider(Long serviceProviderId){
        if (serviceProviderId == null){
            throw new IllegalArgumentException("Service Provider ID cannot be null");
        }

        List<ProviderExceptions> exceptions =  providerExceptionsRepository.findAllByServiceProviderId(serviceProviderId);

        return availabilityMapper.toProviderExceptionDtoList(exceptions);
    }

    public List<ProviderExceptionDto> getAllExceptionsForProviderAndService(Long serviceProviderId,
                                                                        Long serviceId){
        if (serviceProviderId == null || serviceId == null){
            throw new IllegalArgumentException("Service Provider ID and Service ID cannot be null");
        }

        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndServiceId(serviceProviderId, serviceId);

        return availabilityMapper.toProviderExceptionDtoList(exceptions);
    }

    public DayOfWeek[] getAvailableDaysOfWeekForRule(Long serviceId){
        AvailabilityRules rule = availabilityRulesRepository.findByServiceId(serviceId)
                .orElseThrow(()-> new EntityNotFoundException("Availability rule not found"));
        return rule.getDaysOfWeek();
    }

    public ProviderExceptionDto updateException(Long exceptionId, ProviderExceptionDto dto){
        ProviderExceptions exception = providerExceptionsRepository.findById(exceptionId)
                .orElseThrow(()-> new EntityNotFoundException("Provider exception not found"));

        if(dto.getServiceId() != null) exception.setServiceId(dto.getServiceId());
        if(dto.getExceptionDate() != null) exception.setExceptionDate(LocalDate.parse(dto.getExceptionDate()));
        if(dto.getNewStartTime() != null) exception.setNewStartTime(LocalTime.parse(dto.getNewStartTime()));
        if(dto.getNewEndTime() != null) exception.setNewEndTime(LocalTime.parse(dto.getNewEndTime()));
        if(dto.getExceptionReason() != null) exception.setExceptionReason(dto.getExceptionReason());
        if(dto.getExceptionType() != null) exception.setExceptionType(dto.getExceptionType());

        ProviderExceptions updatedException = providerExceptionsRepository.save(exception);
        return availabilityMapper.ProviderExceptionToDto(updatedException);
    }

    public AvailabilityRulesDto updateRules(Long id, AvailabilityRulesDto dto){
        AvailabilityRules rule = availabilityRulesRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Availability rule not found"));

        if(dto.getServiceId() != null) rule.setServiceId(dto.getServiceId());
        if(dto.getStartTime() != null) rule.setStartTime(LocalTime.parse(dto.getStartTime()));
        if(dto.getEndTime() != null) rule.setEndTime(LocalTime.parse(dto.getEndTime()));
        if(dto.getDaysOfWeek() != null) rule.setDaysOfWeek(dto.getDaysOfWeek());

        AvailabilityRules updatedRule = availabilityRulesRepository.save(rule);
        return availabilityMapper.AvailabilityToDto(updatedRule);
    }

    public void deleteAvailabilityRule(Long id){
        AvailabilityRules rule = availabilityRulesRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Availability rule not found"));
        availabilityRulesRepository.delete(rule);
    }

    public void deleteProviderException(Long id){
        ProviderExceptions exception = providerExceptionsRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Provider exception not found"));
        providerExceptionsRepository.delete(exception);
    }


    public AvailabilityStatusResponse checkAvailability(AvailabilityStatusRequest request){
        Long serviceProviderId = request.getServiceProviderId();

        if(request.getStartTime() == null || request.getEndTime() == null || request.getDate() == null){
            throw new InvalidTimeSlotParametersException("Invalid request: start time, end time, and date must be provided");
        }

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findAllByServiceProviderIdAndServiceId(serviceProviderId,
                request.getServiceId());

        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDate(serviceProviderId, request.getDate());

        AvailabilityStatusResponse response = new AvailabilityStatusResponse(
                serviceProviderId,
                request.getStartTime().toString(),
                request.getEndTime().toString(),
                request.getDate().toString(),
                Status.OUTSIDE_WORKING_HOURS
        );

        Boolean isInException = checkTimeInAnyException(request, response, exceptions);
        if(isInException) {
            return response;
        }else if(response.getStatus().equals(Status.BLOCKED)){
            return response;
        }else{
            DayOfWeek day = request.getDate().getDayOfWeek();
            for(AvailabilityRules rule : rules){
                if(rule.isAvailableOn(day)){
                    Boolean isWithinRange = isWithinTimeRange(request.getStartTime(), request.getEndTime(),
                            rule.getStartTime(), rule.getEndTime());
                    if(isWithinRange){
                        response.setStatus(Status.AVAILABLE);
                        return response;
                    }
                }
            }
        }

        return response;
    }

    private Boolean checkTimeInAnyException(AvailabilityStatusRequest request, AvailabilityStatusResponse response,
                                            List<ProviderExceptions> exceptions) {
        for(ProviderExceptions exception : exceptions){
            if(exception.getExceptionDate() == null || exception.getNewStartTime() == null || exception.getNewEndTime() == null || !exception.getExceptionDate().equals(request.getDate())) {
                continue;
            }
            Boolean isWithinRange = isWithinTimeRange(request.getStartTime(), request.getEndTime(),
                    exception.getNewStartTime(), exception.getNewEndTime());
            if(isWithinRange) {
                if (exception.getExceptionType() == ExceptionType.OVERRIDE) {
                    response.setStatus(Status.AVAILABLE);
                    return true;
                } else if (exception.getExceptionType() == ExceptionType.BLOCKED) {
                    response.setStatus(Status.BLOCKED);
                    return false;
                }
            }
        }
        return false;
    }

    private Boolean isWithinTimeRange(LocalTime requestStartTime, LocalTime requestEndTime, LocalTime start,
                                      LocalTime end) {
        boolean startTimeCheck = !requestStartTime.isBefore(start) && !requestStartTime.isAfter(end);
        boolean endTimeCheck = requestEndTime.isAfter(start) && requestEndTime.isBefore(end);;
        return startTimeCheck && endTimeCheck;
    }


}
