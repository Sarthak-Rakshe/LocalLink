package com.sarthak.AvailabilityService.service;

import com.sarthak.AvailabilityService.client.BookingClient;
import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.BookedSlotsResponse;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.dto.Slot;
import com.sarthak.AvailabilityService.dto.request.DayAndTimeAvailabilityRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilitySlotsResponse;
import com.sarthak.AvailabilityService.exception.ConflictingRulesException;
import com.sarthak.AvailabilityService.exception.DuplicateEntityException;
import com.sarthak.AvailabilityService.exception.EntityNotFoundException;
import com.sarthak.AvailabilityService.exception.InvalidTimeSlotParametersException;
import com.sarthak.AvailabilityService.exception.ServiceClientResponseMismatchException;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AvailabilityService{

    private final AvailabilityRulesRepository availabilityRulesRepository;
    private final ProviderExceptionsRepository providerExceptionsRepository;
    private final AvailabilityMapper availabilityMapper;
    private final BookingClient bookingClient;

    public AvailabilityService(AvailabilityRulesRepository availabilityRulesRepository,
                               ProviderExceptionsRepository providerExceptionsRepository,
                               AvailabilityMapper availabilityMapper,
                               BookingClient bookingClient) {
        this.availabilityRulesRepository = availabilityRulesRepository;
        this.providerExceptionsRepository = providerExceptionsRepository;
        this.availabilityMapper = availabilityMapper;
        this.bookingClient = bookingClient;
    }


    public AvailabilityRulesDto createAvailabilityRule(AvailabilityRulesDto availabilityRulesDto) {
        log.info("Creating availability rule for Service Provider ID: {}, Service ID: {}",
                availabilityRulesDto.getServiceProviderId(), availabilityRulesDto.getServiceId());
        AvailabilityRules rule = availabilityMapper.AvailabilityDtoToEntity(availabilityRulesDto);

        Optional<AvailabilityRules> existingRule = availabilityRulesRepository
                .findByServiceProviderIdAndServiceIdAndStartTimeAndEndTime(rule.getServiceProviderId(),
                        rule.getServiceId(), rule.getStartTime(), rule.getEndTime());

        if (existingRule.isPresent()) {
            log.info("Duplicate rule found for Service Provider ID: {}, Service ID: {}, Start Time: {}, End Time: {}, Days of Week: {}",
                    availabilityRulesDto.getServiceProviderId(), availabilityRulesDto.getServiceId(),
                    availabilityRulesDto.getStartTime(), availabilityRulesDto.getEndTime(), availabilityRulesDto.getDaysOfWeek());
            throw new DuplicateEntityException("An availability rule with the same parameters already exists.");
        }

        byte daysOfWeek = 0;
        for(DayOfWeek day : rule.getDaysOfWeek()){
            daysOfWeek |= (byte) (1 << day.getValue());
        }
        List<AvailabilityRules> conflictingRules = availabilityRulesRepository
                .findConflictingRules(rule.getServiceProviderId(), rule.getServiceId(),
                        rule.getStartTime(), rule.getEndTime(), daysOfWeek);

        if(!conflictingRules.isEmpty()){
            log.error("Conflicting availability rules found for Service Provider ID: {}, Service ID: {}",
                    availabilityRulesDto.getServiceProviderId(), availabilityRulesDto.getServiceId());
            throw new ConflictingRulesException("Conflicting availability rules exist for the given time slot and days.");
        }
        AvailabilityRules savedRule = availabilityRulesRepository.save(rule);
        log.info("Availability rule created with ID: {}", savedRule.getRuleId());
        return availabilityMapper.AvailabilityToDto(savedRule);
    }

    public ProviderExceptionDto createProviderException(ProviderExceptionDto dto){
        validateDate(LocalDate.parse(dto.getExceptionDate()));
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
            throw new DuplicateEntityException("An exception for the same date and time already exists.");
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

    public AvailabilitySlotsResponse getAvailabilitySlots(Long serviceProviderId,
                                                         Long serviceId,
                                                         LocalDate date){
        if(serviceProviderId == null || serviceId == null || date == null){
            throw new IllegalArgumentException("Service Provider ID, Service ID, and Date must be provided");
        }
        validateDate(date);
        AvailabilitySlotsResponse response = AvailabilitySlotsResponse.builder()
                .date(date)
                .availableSlots(new ArrayList<>())
                .isDayAvailable(false)
                .build();

        byte day = (byte) (date.getDayOfWeek().getValue() % 7 == 0 ? 0 : date.getDayOfWeek().getValue());

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(
                        serviceProviderId,
                        serviceId,
                        day);

        if (rules.isEmpty()){
            log.info("No availability rules found for Service Provider ID: {}, Service ID: {}, Date: {}",
                    serviceProviderId, serviceId, date);
            return response;
        }

        log.info("Found {} availability rules for Service Provider ID: {}, Service ID: {}, Date: {}",
                rules.size(), serviceProviderId, serviceId, date);

        List<Slot> mergedSlots = mergeRules(rules);
        log.debug("Merged availability slots from rules: {}", mergedSlots);

        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(
                        serviceProviderId,
                        date
                );

        if (!exceptions.isEmpty()){
            log.info("Found {} provider exceptions for Service Provider ID: {}, Date: {}",
                    exceptions.size(), serviceProviderId, date);
            mergedSlots = mergeExceptions(mergedSlots, exceptions);
            log.debug("Merged availability slots after applying exceptions: {}", mergedSlots);
        }

        if(mergedSlots.isEmpty()){
            log.info("No available slots after applying exceptions for Service Provider ID: {}, Service ID: {}, Date: {}",
                    serviceProviderId, serviceId, date);
            return response;
        }

        // Fetch booked slots from Booking Service(Includes pending and confirmed bookings)
        BookedSlotsResponse bookedSlotsResponse = bookingClient.getBookedSlotsForProviderOnDate(
                serviceProviderId,
                serviceId,
                date
        );
        log.info("Fetched merged booked slots for pending and confirmed bookings, size of response list is {}",
                bookedSlotsResponse.bookedSlots().size());

        List<Slot> bookedSlots = validatedBookedSlots(bookedSlotsResponse, serviceProviderId,
                serviceId, date);

        mergedSlots = mergeBookedSlots(mergedSlots, bookedSlots).stream()
                .filter( s -> Duration.between(s.startTime(), s.endTime()).toMinutes() > 10)
                .toList();
        log.debug("Final available slots after merging booked slots: {}", mergedSlots);

        boolean isDayAvailable = !mergedSlots.isEmpty();

        log.info("Final available slots for Service Provider ID: {}, Service ID: {}, Date: {}: {}",
                serviceProviderId, serviceId, date, mergedSlots);

        return AvailabilitySlotsResponse.builder()
                .date(date)
                .availableSlots(mergedSlots)
                .isDayAvailable(isDayAvailable)
                .build();
    }

    private List<Slot> mergeBookedSlots(List<Slot> finalSlots, List<Slot> bookedSlots) {
        if(bookedSlots.isEmpty()) return finalSlots;

        List<Slot> availableSlots = new ArrayList<>();

        List<Slot> sortedBookedSlots = bookedSlots.stream()
                .sorted(Comparator.comparing(Slot::startTime))
                .toList();

        //Noting point is that all booked slots are strictly within available slots
        for(Slot available : finalSlots){
            LocalTime availableStart = available.startTime();
            LocalTime availableEnd = available.endTime();


            for(Slot booked : sortedBookedSlots){
                LocalTime bookedStart = booked.startTime();
                LocalTime bookedEnd = booked.endTime();

                if(bookedStart.isBefore(availableEnd) && bookedEnd.isAfter(availableStart)) {
                    if(availableStart.isBefore(bookedStart)){
                        availableSlots.add(new Slot(availableStart, bookedStart));
                    }

                    availableStart = bookedEnd;
                }
            }
            if(availableStart.isBefore(availableEnd)){
                availableSlots.add(new Slot(availableStart, availableEnd));
            }
        }

        return availableSlots;
    }

    private List<Slot> validatedBookedSlots(BookedSlotsResponse bookedSlotsResponse, Long serviceProviderId,
                                           Long serviceId, LocalDate date) {
        if(bookedSlotsResponse == null){
            log.debug("Received null booked slots response from Booking Service for Service Provider ID: {}, Service " +
                            "ID: {}, Date: {}",
                    serviceProviderId, serviceId, date);
            return new ArrayList<>();
        }
        if(!bookedSlotsResponse.serviceProviderId().equals(serviceProviderId) ||
                !bookedSlotsResponse.serviceId().equals(serviceId) ||
                !LocalDate.parse(bookedSlotsResponse.date()).equals(date)){
            log.error("Mismatch in booked slots response data. Expected Service Provider ID: {}, Service ID: {}, Date: {}. " +
                            "Received Service Provider ID: {}, Service ID: {}, Date: {}",
                    serviceProviderId, serviceId, date,
                    bookedSlotsResponse.serviceProviderId(), bookedSlotsResponse.serviceId(),
                    bookedSlotsResponse.date());
            throw new ServiceClientResponseMismatchException("Mismatch in booked slots response data");
        }
        return bookedSlotsResponse.bookedSlots() != null ? bookedSlotsResponse.bookedSlots() : new ArrayList<>();
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
        validateDate(LocalDate.parse(dto.getExceptionDate()));
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
        validateDate(request.date());
        log.info("Checking availability for Service Provider ID: {}, Service ID: {}, Date: {}, Start Time: {}, End Time: {}",
                request.serviceProviderId(), request.serviceId(), request.date(), request.startTime(), request.endTime());

        Long serviceProviderId = request.serviceProviderId();

        if(request.startTime() == null || request.endTime() == null || request.date() == null){
            log.error("Invalid request: start time {}, end time {}, and date {} provided",
                    request.startTime(), request.endTime(), request.date());
            throw new InvalidTimeSlotParametersException("Invalid request: start time, end time, and date must be provided");
        }

        AvailabilityStatusResponse response = new AvailabilityStatusResponse(
                serviceProviderId,
                request.startTime().toString(),
                request.endTime().toString(),
                request.date().toString(),
                Status.OUTSIDE_WORKING_HOURS
        );

        byte dayOfWeek = (byte) (request.date().getDayOfWeek().getValue() == 7 ? 0 : request.date().getDayOfWeek().getValue());

        List<AvailabilityRules> rules =
                availabilityRulesRepository.findByServiceProviderAndServiceAndDayOrdered(serviceProviderId,
                request.serviceId(), dayOfWeek);

        log.info("Found {} availability rules for Service Provider ID: {}, Service ID: {}, Day of Week: {}",
                rules.size(), serviceProviderId, request.serviceId(), dayOfWeek);


        List<ProviderExceptions> exceptions =
                providerExceptionsRepository.findAllByServiceProviderIdAndExceptionDateOrderByNewStartTimeAsc(serviceProviderId, request.date());

        log.info("Found {} provider exceptions for Service Provider ID: {}, Date: {}",
                exceptions.size(), serviceProviderId, request.date());

        if (!exceptions.isEmpty()){
            boolean exceptionResult = checkTimeInAnyException(request, response, exceptions);

            if (exceptionResult){
                log.info("Availability determined by exception for Service Provider ID: {}, Date: {}",
                        serviceProviderId, request.date());
                return response;
            }
        }

        log.info("No applicable exceptions found or exceptions do not determine availability for Service Provider ID: {}, Date: {}. Checking regular availability rules.",
                serviceProviderId, request.date());

        DayOfWeek day = request.date().getDayOfWeek();

        for (AvailabilityRules rule : rules){
            if(rule.isAvailableOn(day)){
                Boolean isWithinRange = isWithinTimeRange(request.startTime(), request.endTime(),
                        rule.getStartTime(), rule.getEndTime());

                if(isWithinRange){
                    response.setStatus(Status.AVAILABLE);
                    log.info("Service Provider ID: {} is AVAILABLE on Date: {} from {} to {} as per rule ID: {}",
                            serviceProviderId, request.date(), request.startTime(), request.endTime(),
                            rule.getRuleId());
                    return response;
                }
            }
        }

        log.info("Service Provider ID: {} is NOT AVAILABLE on Date: {} from {} to {}",
                serviceProviderId, request.date(), request.startTime(), request.endTime());

        return response;
    }

    private boolean checkTimeInAnyException(AvailabilityStatusRequest request, AvailabilityStatusResponse response,
                                            List<ProviderExceptions> exceptions) {
        log.info("Checking for applicable exceptions on Date: {} for Service Provider ID: {}",
                request.date(), request.serviceProviderId());

        for(ProviderExceptions exception : exceptions){
            if(exception.getExceptionDate() == null ||
                    exception.getNewStartTime() == null ||
                    exception.getNewEndTime() == null ||
                    !exception.getExceptionDate().equals(request.date())) {

                log.info("Skipping exception ID: {} due to missing or non-matching date/time fields",
                        exception.getExceptionId());

                continue;
            }
            if (exception.getExceptionType() == ExceptionType.BLOCKED) {
                boolean hasOverlap = hasTimeOverlap(request.startTime(), request.endTime(),
                        exception.getNewStartTime(), exception.getNewEndTime());

                if(hasOverlap){
                    response.setStatus(Status.BLOCKED);

                    log.info("Exception ID: {} BLOCKS availability for Service Provider ID: {} on Date: {}" +
                            "due to overlap between request time {} - {} and exception time {} - {}",
                            exception.getExceptionId(), request.serviceProviderId(), request.date(),
                            request.startTime(), request.endTime(),
                            exception.getNewStartTime(), exception.getNewEndTime());

                    return true;

                }
            } else if (exception.getExceptionType() == ExceptionType.OVERRIDE) {
                boolean hasIntersection = hasTimeIntersection(request.startTime(), request.endTime(),
                        exception.getNewStartTime(), exception.getNewEndTime());

                if (hasIntersection) {
                    if (!isFullyCovered(request.startTime(), request.endTime(),
                            exception.getNewStartTime(), exception.getNewEndTime())) {
                        log.info("Given time slot {} to {} is not Fully covered by OVERRIDE exception ID: {}" +
                                        " time slot {} to {} for Service Provider ID: {} on Date: {}",
                                request.startTime(), request.endTime(),
                                exception.getExceptionId(),
                                exception.getNewStartTime(), exception.getNewEndTime(),
                                request.serviceProviderId(), request.date());

                        response.setStatus(Status.OUTSIDE_WORKING_HOURS);
                        return true;
                    }

                    response.setStatus(Status.AVAILABLE);
                    log.info("Exception ID: {} OVERRIDES availability for Service Provider ID: {} on Date: {}",
                            exception.getExceptionId(), request.serviceProviderId(), request.date());
                    return true;
                }

            }

        }
        log.info("No applicable exceptions found for Service Provider ID: {} on Date: {}",
                request.serviceProviderId(), request.date());
        return false;
    }

    private boolean hasTimeIntersection(LocalTime requestStart, LocalTime requestEnd, LocalTime exceptionStart,
                                        LocalTime exceptionEnd){
        log.info("Checking time intersection between request time {} - {} and exception time {} - {}",
                requestStart, requestEnd, exceptionStart, exceptionEnd);

        return !requestEnd.isBefore(exceptionStart) && !requestStart.isAfter(exceptionEnd);
    }

    private boolean isFullyCovered(LocalTime requestStart, LocalTime requestEnd,
                                   LocalTime exceptionStart, LocalTime exceptionEnd) {
        log.info("Checking if request time {} - {} is fully covered by exception time {} - {}",
                requestStart, requestEnd, exceptionStart, exceptionEnd);

        boolean fullyCovered = !exceptionStart.isAfter(requestStart) && !exceptionEnd.isBefore(requestEnd);
        log.info("Fully covered result: {}", fullyCovered);
        return fullyCovered;
    }

    private boolean hasTimeOverlap(LocalTime requestStart, LocalTime requestEnd,
                                   LocalTime exceptionStart, LocalTime exceptionEnd) {
        log.info("Checking for overlap between request time {} - {} and exception time {} - {}",
                requestStart, requestEnd, exceptionStart, exceptionEnd);

        // No overlap if request ends exactly when exception starts, or vice versa
        boolean noOverlap = requestEnd.equals(exceptionStart) || requestStart.equals(exceptionEnd) ||
                requestEnd.isBefore(exceptionStart) || requestStart.isAfter(exceptionEnd);

        boolean hasOverlap = !noOverlap;
        log.info("Overlap result: {}", hasOverlap);
        return hasOverlap;
    }

    private Boolean isWithinTimeRange(LocalTime requestStartTime, LocalTime requestEndTime,
                                      LocalTime ruleStartTime, LocalTime ruleEndTime) {
        log.info("Checking if request time {} - {} is within availability rule {} - {}",
                requestStartTime, requestEndTime, ruleStartTime, ruleEndTime);

        boolean startTimeCheck = !requestStartTime.isBefore(ruleStartTime) && !requestStartTime.isAfter(ruleEndTime);
        boolean endTimeCheck = !requestEndTime.isBefore(ruleStartTime) && !requestEndTime.isAfter(ruleEndTime);

        boolean isWithinRange = startTimeCheck && endTimeCheck;
        log.info("Within range result: {} (start check: {}, end check: {})", isWithinRange, startTimeCheck, endTimeCheck);

        return isWithinRange;
    }

    private void validateDate(LocalDate providedDate){
        if(providedDate.isBefore(LocalDate.now())){
            log.error("Provided date {} is in the past", providedDate);
            throw new IllegalArgumentException("Date cannot be in the past");
        }
    }
}

