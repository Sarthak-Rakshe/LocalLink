package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.dto.Slot;
import com.sarthak.AvailabilityService.dto.request.AvailabilitySlotsRequest;
import com.sarthak.AvailabilityService.dto.request.DayAndTimeAvailabilityRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilitySlotsResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        log.info("Creating availability rule for Service Provider ID: {}, Service ID: {}",
                dto.getServiceProviderId(), dto.getServiceId());
        AvailabilityRules rule = availabilityMapper.AvailabilityDtoToEntity(dto);

        Optional<AvailabilityRules> existingRule = availabilityRulesRepository
                .findByServiceProviderIdAndServiceIdAndStartTimeAndEndTime(rule.getServiceProviderId(),
                        rule.getServiceId(), rule.getStartTime(), rule.getEndTime());

        if (existingRule.isPresent()) {
            log.info("Duplicate availability rule found for Service Provider ID: {}, Service ID: {}",
                    dto.getServiceProviderId(), dto.getServiceId());
            throw new DuplicateEntityException("An availability rule with the same parameters already exists.");
        }

        AvailabilityRules savedRule = availabilityRulesRepository.save(rule);
        log.info("Availability rule created with ID: {}", savedRule.getRuleId());
        return availabilityMapper.AvailabilityToDto(savedRule);
    }

    public ProviderExceptionDto createProviderException(ProviderExceptionDto dto){
        ProviderExceptions exception = availabilityMapper.DtoToProviderException(dto);
        log.info("Creating provider exception for Service Provider ID: {}, Service ID: {}, Date: {}",
                dto.getServiceProviderId(), dto.getServiceId(), dto.getExceptionDate());

        Optional<ProviderExceptions> existingException = providerExceptionsRepository
                .findAllByServiceProviderIdAndServiceIdAndExceptionDateAndNewStartTimeAndNewEndTime(exception.getServiceProviderId(),
                        exception.getServiceId(), exception.getExceptionDate(), exception.getNewStartTime(),
                        exception.getNewEndTime());
        if (existingException.isPresent()) {
            log.error("Duplicate provider exception found for Service Provider ID: {}, Service ID: {}, Date: {}",
                    dto.getServiceProviderId(), dto.getServiceId(), dto.getExceptionDate());
            throw new DuplicateEntityException("An exception for the same date " +
                    "and time already exists.");
        }

        ProviderExceptions savedException = providerExceptionsRepository.save(exception);
        log.info("Provider exception created with ID: {}", savedException.getExceptionId());
        return availabilityMapper.ProviderExceptionToDto(savedException);
    }


    public Page<AvailabilityRulesDto> getAllAvailabilityRulesOnDayAndTime(DayAndTimeAvailabilityRequest request,
                                                              int page, int size){
        log.info("Fetching availability rules for Day: {}, Start Time: {}, End Time: {}, Page: {}, Size: {}",
                request.day(), request.startTime(), request.endTime(), page, size);

         if(request.startTime() == null || request.endTime() == null){
             throw new InvalidTimeSlotParametersException("Start time and end time must be provided");
         }

         if(request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())){
             throw new InvalidTimeSlotParametersException("Start time must be before end time");
         }

         if(page < 0 || size <= 0){
             throw new IllegalArgumentException("Page index must be non-negative and size must be positive");
         }

         if(request.day() == null){
             throw new IllegalArgumentException("Day of the week must be provided");
         }
        PageRequest pr = PageRequest.of(page, size);
        DayOfWeek dayOfWeek = request.day();
        LocalTime startTime = request.startTime().truncatedTo(ChronoUnit.SECONDS);
        LocalTime endTime = request.endTime().truncatedTo(ChronoUnit.SECONDS);

        Page<AvailabilityRules> rules =  availabilityRulesRepository.findAvailableOnDayAndTime(dayOfWeek.getValue(),
                startTime, endTime, pr);

        log.info("Found {} availability rules for Day: {}, Start Time: {}, End Time: {}",
                rules.getNumberOfElements(), request.day(), request.startTime(), request.endTime());

        return rules.map(availabilityMapper::AvailabilityToDto);
    }

    public List<AvailabilityRulesDto> getAllAvailabilityRulesForProvider(Long serviceProviderId){
        if (serviceProviderId == null){
            throw new IllegalArgumentException("Service Provider ID cannot be null");
        }
        log.info("Fetching all availability rules for Service Provider ID: {}", serviceProviderId);

        List<AvailabilityRules> rules =  availabilityRulesRepository.findAllByServiceProviderId(serviceProviderId);
        log.info("Found {} availability rules for Service Provider ID: {}",
                rules.size(), serviceProviderId);
        return availabilityMapper.toAvailabilityDtoList(rules);
    }

    public List<AvailabilityRulesDto> getAllAvailabilityRulesForProviderAndService(Long serviceProviderId,
                                                                                 Long serviceId){
        if (serviceProviderId == null || serviceId == null){
            throw new IllegalArgumentException("Service Provider ID and Service ID cannot be null");
        }

        log.info("Fetching all availability rules for Service Provider ID: {}, Service ID: {}",
                serviceProviderId, serviceId);

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findAllByServiceProviderIdAndServiceId(serviceProviderId, serviceId);

        log.info("Found {} availability rules for Service Provider ID: {}, Service ID: {}",
                rules.size(), serviceProviderId, serviceId);
        return availabilityMapper.toAvailabilityDtoList(rules);
    }

    public List<ProviderExceptionDto> getAllExceptionsForProvider(Long serviceProviderId){
        if (serviceProviderId == null){
            throw new IllegalArgumentException("Service Provider ID cannot be null");
        }

        log.info("Fetching all provider exceptions for Service Provider ID: {}", serviceProviderId);

        List<ProviderExceptions> exceptions =  providerExceptionsRepository.findAllByServiceProviderId(serviceProviderId);

        log.info("Found {} provider exceptions for Service Provider ID: {}",
                exceptions.size(), serviceProviderId);

        return availabilityMapper.toProviderExceptionDtoList(exceptions);
    }

    public List<ProviderExceptionDto> getAllExceptionsForProviderAndService(Long serviceProviderId,
                                                                        Long serviceId){
        if (serviceProviderId == null || serviceId == null){
            throw new IllegalArgumentException("Service Provider ID and Service ID cannot be null");
        }

        log.info("Fetching all provider exceptions for Service Provider ID: {}, Service ID: {}",
                serviceProviderId, serviceId);

        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndServiceId(serviceProviderId, serviceId);

        log.info("Found {} provider exceptions for Service Provider ID: {}, Service ID: {}",
                exceptions.size(), serviceProviderId, serviceId);
        return availabilityMapper.toProviderExceptionDtoList(exceptions);
    }

    public DayOfWeek[] getAvailableDaysOfWeekForRule(Long serviceId){
        if (serviceId == null){
            throw new IllegalArgumentException("Service ID cannot be null");
        }
        log.info("Fetching available days of week for Service ID: {}", serviceId);
        AvailabilityRules rule = availabilityRulesRepository.findByServiceId(serviceId)
                .orElseThrow(()->{
                        log.error("Availability rule not found for Service ID: {}", serviceId);
                        return new EntityNotFoundException("Availability rule not found");
                });
        return rule.getDaysOfWeek();
    }

    public AvailabilitySlotsResponse getAvailabilitySlots(AvailabilitySlotsRequest request){
        if(request.serviceProviderId() == null || request.serviceId() == null || request.date() == null){
            throw new IllegalArgumentException("Service Provider ID, Service ID, and Date must be provided");
        }
        byte day = (byte) (request.date().getDayOfWeek().getValue() % 7);
        List<AvailabilityRules> rules =
                availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(
                        request.serviceProviderId(),
                        request.serviceId(),
                        day);

        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(
                        request.serviceProviderId(),
                        request.date()
                );




    }

    private List<Slot> mergeRules(List<AvailabilityRules> rules){
        List<Slot> slots = new ArrayList<>();
        if (rules.isEmpty()) return slots;

        LocalTime tempStartTime = rules.getFirst().getStartTime();
        LocalTime tempEndTime = rules.getFirst().getEndTime();

        for (int i = 1; i < rules.size(); i++) {
            AvailabilityRules currentRule = rules.get(i);

            if (!currentRule.getStartTime().isAfter(tempEndTime)) {
                // Overlapping or contiguous intervals, extend the end time if needed
                if (currentRule.getEndTime().isAfter(tempEndTime)) {
                    tempEndTime = currentRule.getEndTime();
                }
            } else {
                // Non-overlapping interval, add the previous interval and reset
                slots.add(new Slot(tempStartTime, tempEndTime));

                tempStartTime = currentRule.getStartTime();
                tempEndTime = currentRule.getEndTime();
            }
        }
        slots.add(new Slot(tempStartTime, tempEndTime));

        return slots;
    }

    private List<Slot> mergeExceptions(List<Slot> rules, List<ProviderExceptions> exceptions){
        if(exceptions.isEmpty()) return rules;

        List<Slot> currentSlots = new ArrayList<>(rules);

        for(ProviderExceptions ex : exceptions){
            LocalTime exStart = ex.getNewStartTime();
            LocalTime exEnd = ex.getNewEndTime();
            ExceptionType type = ex.getExceptionType();
            
            List<Slot> tempSlots = new ArrayList<>();
            
            for(Slot slot : currentSlots){
                LocalTime slotStart = slot.startTime();
                LocalTime slotEnd = slot.endTime();
                
                if(exEnd.isBefore(slotStart) || exStart.isAfter(slotEnd)){
                    // No overlap
                    tempSlots.add(slot);
                    continue;
                } 
                
                if (type == ExceptionType.BLOCKED){
                    if(exStart.isAfter(slotStart) && exEnd.isBefore(slotEnd)){
                        tempSlots.add(new Slot(slotStart, exStart));
                        tempSlots.add(new Slot(exEnd, slotEnd));
                    } else if (!exStart.isAfter(slotStart) && !exEnd.isBefore(slotEnd)) {
                        //covers entire slot, remove it
                        continue;
                    } else if (!exStart.isAfter(slotStart)) {
                        tempSlots.add(new Slot(exEnd, slotEnd));
                    } else {
                        tempSlots.add(new Slot(slotStart, exStart));
                    }
                } else if (type == ExceptionType.OVERRIDE) {
                    //merge override
                    LocalTime newStart = slotStart.isBefore(exStart) ? slotStart : exStart;
                    LocalTime newEnd = slotEnd.isAfter(exEnd) ? slotEnd : exEnd;
                    tempSlots.add(new Slot(newStart, newEnd));
                }
            }
            currentSlots = tempSlots;
        }
        rules = mergeOverlappingSlots(currentSlots);

        return rules;
    }

    private List<Slot> mergeOverlappingSlots(List<Slot> slots) {
        if (slots.isEmpty()) return slots;

        List<Slot> mergedSlots = new ArrayList<>();
        Slot current = slots.getFirst();

        for (int i = 1; i < slots.size(); i++){
            Slot next = slots.get(i);

            if(!next.startTime().isAfter(current.endTime())){
                current = new Slot(current.startTime(),
                        next.endTime().isAfter(current.endTime()) ? next.endTime() : current.endTime());
            }else {
                mergedSlots.add(current);
                current = next;
            }
        }

        mergedSlots.add(current);
        return mergedSlots;
    }


    public ProviderExceptionDto updateException(Long exceptionId, ProviderExceptionDto dto){
        log.info("Updating provider exception with ID: {}", exceptionId);
        if (exceptionId == null || exceptionId <= 0) {
            throw new IllegalArgumentException("Exception ID must be a positive number");
        }
        ProviderExceptions exception = providerExceptionsRepository.findById(exceptionId)
                .orElseThrow(()-> new EntityNotFoundException("Provider exception not found"));

        log.info("Found provider exception with ID: {}", exceptionId);
        if(dto.getServiceId() != null) exception.setServiceId(dto.getServiceId());
        if(dto.getExceptionDate() != null) exception.setExceptionDate(LocalDate.parse(dto.getExceptionDate()));
        if(dto.getNewStartTime() != null) exception.setNewStartTime(LocalTime.parse(dto.getNewStartTime()));
        if(dto.getNewEndTime() != null) exception.setNewEndTime(LocalTime.parse(dto.getNewEndTime()));
        if(dto.getExceptionReason() != null) exception.setExceptionReason(dto.getExceptionReason());
        if(dto.getExceptionType() != null) exception.setExceptionType(dto.getExceptionType());

        log.info("Updated fields for provider exception with ID: {}", exceptionId);

        ProviderExceptions updatedException = providerExceptionsRepository.save(exception);

        log.info("Saved updated provider exception with ID: {}", exceptionId);

        return availabilityMapper.ProviderExceptionToDto(updatedException);
    }

    public AvailabilityRulesDto updateRules(Long id, AvailabilityRulesDto dto){
        log.info("Updating availability rule with ID: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Rule ID must be a positive number");
        }
        AvailabilityRules rule = availabilityRulesRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Availability rule not found"));

        log.info("Found availability rule with ID: {}", id);

        if(dto.getServiceId() != null) rule.setServiceId(dto.getServiceId());
        if(dto.getStartTime() != null) rule.setStartTime(LocalTime.parse(dto.getStartTime()));
        if(dto.getEndTime() != null) rule.setEndTime(LocalTime.parse(dto.getEndTime()));
        if(dto.getDaysOfWeek() != null) rule.setDaysOfWeek(dto.getDaysOfWeek());

        log.info("Updated fields for availability rule with ID: {}", id);

        AvailabilityRules updatedRule = availabilityRulesRepository.save(rule);

        log.info("Saved updated availability rule with ID: {}", id);
        return availabilityMapper.AvailabilityToDto(updatedRule);
    }

    public void deleteAvailabilityRule(Long id){
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Rule ID must be a positive number");
        }
        log.info("Deleting availability rule with ID: {}", id);
        AvailabilityRules rule = availabilityRulesRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Availability rule not found"));

        availabilityRulesRepository.delete(rule);
        log.info("Deleted availability rule with ID: {}", id);
    }

    public void deleteProviderException(Long id){
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Exception ID must be a positive number");
        }
        log.info("Deleting provider exception with ID: {}", id);
        ProviderExceptions exception = providerExceptionsRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Provider exception not found"));
        providerExceptionsRepository.delete(exception);
        log.info("Deleted provider exception with ID: {}", id);
    }


    public AvailabilityStatusResponse checkAvailability(AvailabilityStatusRequest request){
        if(request == null){
            throw new IllegalArgumentException("Request cannot be null");
        }
        log.info("Checking availability for Service Provider ID: {}, Service ID: {}, Date: {}, Start Time: {}, End Time: {}",
                request.getServiceProviderId(), request.getServiceId(), request.getDate(),
                request.getStartTime(), request.getEndTime());

        Long serviceProviderId = request.getServiceProviderId();

        if(request.getStartTime() == null || request.getEndTime() == null || request.getDate() == null){
            log.error("Invalid request: start time {}, end time {}, and date {} provided",
                    request.getStartTime(), request.getEndTime(), request.getDate());
            throw new InvalidTimeSlotParametersException("Invalid request: start time, end time, and date must be provided");
        }

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findAllByServiceProviderIdAndServiceId(serviceProviderId,
                request.getServiceId());

        log.info("Found {} availability rules for Service Provider ID: {}, Service ID: {}",
                rules.size(), serviceProviderId, request.getServiceId());

        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(serviceProviderId, request.getDate());
        log.info("Found {} provider exceptions for Service Provider ID: {}, Date: {}",
                exceptions.size(), serviceProviderId, request.getDate());

        AvailabilityStatusResponse response = new AvailabilityStatusResponse(
                serviceProviderId,
                request.getStartTime().toString(),
                request.getEndTime().toString(),
                request.getDate().toString(),
                Status.OUTSIDE_WORKING_HOURS
        );


        Boolean isInException = checkTimeInAnyException(request, response, exceptions);
        if(isInException) {
            log.info("Availability determined by exception for Service Provider ID: {}, Date: {}",
                    serviceProviderId, request.getDate());
            return response;
        }else if(response.getStatus().equals(Status.BLOCKED)){
            log.info("Availability blocked by exception for Service Provider ID: {}, Date: {}",
                    serviceProviderId, request.getDate());
            return response;
        }else{
            log.info("No applicable exceptions found for Service Provider ID: {}, Date: {}. Checking regular availability rules.",
                    serviceProviderId, request.getDate());
            DayOfWeek day = request.getDate().getDayOfWeek();
            for(AvailabilityRules rule : rules){
                if(rule.isAvailableOn(day)){
                    Boolean isWithinRange = isWithinTimeRange(request.getStartTime(), request.getEndTime(),
                            rule.getStartTime(), rule.getEndTime());
                    if(isWithinRange){
                        response.setStatus(Status.AVAILABLE);
                        log.info("Service Provider ID: {} is AVAILABLE on Date: {} from {} to {}",
                                serviceProviderId, request.getDate(), request.getStartTime(), request.getEndTime());
                        return response;
                    }
                }
            }
        }
        log.info("Service Provider ID: {} is NOT AVAILABLE on Date: {} from {} to {}",
                serviceProviderId, request.getDate(), request.getStartTime(), request.getEndTime());

        return response;
    }

    private Boolean checkTimeInAnyException(AvailabilityStatusRequest request, AvailabilityStatusResponse response,
                                            List<ProviderExceptions> exceptions) {
        log.info("Checking for applicable exceptions on Date: {} for Service Provider ID: {}",
                request.getDate(), request.getServiceProviderId());
        for(ProviderExceptions exception : exceptions){
            if(exception.getExceptionDate() == null ||
                    exception.getNewStartTime() == null ||
                    exception.getNewEndTime() == null ||
                    !exception.getExceptionDate().equals(request.getDate())) {
                log.info("Skipping exception ID: {} due to missing or non-matching date/time fields",
                        exception.getExceptionId());
                continue;
            }
            Boolean isWithinRange = isWithinTimeRange(request.getStartTime(), request.getEndTime(),
                    exception.getNewStartTime(), exception.getNewEndTime());
            if(isWithinRange) {
                if (exception.getExceptionType() == ExceptionType.OVERRIDE) {
                    response.setStatus(Status.AVAILABLE);
                    log.info("Exception ID: {} OVERRIDES availability for Service Provider ID: {} on Date: {}",
                            exception.getExceptionId(), request.getServiceProviderId(), request.getDate());
                    return true;
                } else if (exception.getExceptionType() == ExceptionType.BLOCKED) {
                    response.setStatus(Status.BLOCKED);
                    log.info("Exception ID: {} BLOCKS availability for Service Provider ID: {} on Date: {}",
                            exception.getExceptionId(), request.getServiceProviderId(), request.getDate());
                    return false;
                }
            }
        }
        log.info("No applicable exceptions found for Service Provider ID: {} on Date: {}",
                request.getServiceProviderId(), request.getDate());
        return false;
    }

    private Boolean isWithinTimeRange(LocalTime requestStartTime, LocalTime requestEndTime, LocalTime start,
                                      LocalTime end) {
        log.info("Checking if request time {} - {} is within range {} - {}",
                requestStartTime, requestEndTime, start, end);
        boolean startTimeCheck = !requestStartTime.isBefore(start) && !requestStartTime.isAfter(end);
        boolean endTimeCheck = requestEndTime.isAfter(start) && requestEndTime.isBefore(end);
        log.info("Start time check: {}, End time check: {}", startTimeCheck, endTimeCheck);
        return startTimeCheck && endTimeCheck;
    }


}
