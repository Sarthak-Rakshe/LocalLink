package com.sarthak.AvailabilityService.controller;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.request.DayAndTimeAvailabilityRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilityStatusResponse;
import com.sarthak.AvailabilityService.dto.response.PageResponse;
import com.sarthak.AvailabilityService.service.AvailabilityService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/rules/{serviceProviderId}")
    public ResponseEntity<List<AvailabilityRulesDto>> getAvailabilityRulesForProvider(@PathVariable Long serviceProviderId){
        List<AvailabilityRulesDto> rules = availabilityService.getAllAvailabilityRulesForProvider(serviceProviderId);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/{service-provider-id}/{service-id}")
    public ResponseEntity<List<AvailabilityRulesDto>> getAvailabilityRulesForService(@PathVariable("service-id") Long serviceId,
                                                                                     @PathVariable("service-provider-id") Long providerId){
        List<AvailabilityRulesDto> rules = availabilityService.getAllAvailabilityRulesForProviderAndService(serviceId,providerId);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/exceptions/{serviceProviderId}")
    public ResponseEntity<List<ProviderExceptionDto>> getProviderExceptions(@PathVariable Long serviceProviderId){
        List<ProviderExceptionDto> exceptions = availabilityService.getAllExceptionsForProvider(serviceProviderId);
        return ResponseEntity.ok(exceptions);
    }

    @GetMapping("/exceptions/{service-provider-id}/{service-id}")
    public ResponseEntity<List<ProviderExceptionDto>> getProviderExceptionsForService(@PathVariable("service-id") Long serviceId,
                                                                                     @PathVariable("service-provider-id") Long providerId){
        List<ProviderExceptionDto> exceptions = availabilityService.getAllExceptionsForProviderAndService(serviceId,providerId);
        return ResponseEntity.ok(exceptions);
    }

    @GetMapping("/rules/day-and-time")
    public PageResponse<AvailabilityRulesDto> getAvailabilityRulesForDayAndTime(@RequestBody DayAndTimeAvailabilityRequest request,
                                                                                @RequestParam int page,
                                                                                @RequestParam int size){
        Page<AvailabilityRulesDto> rules = availabilityService.getAllAvailabilityRulesOnDayAndTime(request, page,
                size);

        return new PageResponse<>(
                rules.getContent(),
                rules.getNumber(),
                rules.getTotalElements(),
                rules.getTotalPages(),
                rules.getSize()
        );
    }

    @GetMapping("rules/{service-id}/available-days")
    public ResponseEntity<DayOfWeek[]> getAvailableDaysForService(@PathVariable("service-id") Long serviceId){
        DayOfWeek[] availableDays = availabilityService.getAvailableDaysOfWeekForRule(serviceId);
        return ResponseEntity.ok(availableDays);
    }

    @PostMapping("/rules")
    public ResponseEntity<AvailabilityRulesDto> createAvailabilityRule(@RequestBody AvailabilityRulesDto rule){
        AvailabilityRulesDto savedRule = availabilityService.createAvailabilityRule(rule);
        return ResponseEntity.ok(savedRule);
    }

    @PostMapping("/exceptions")
    public ResponseEntity<ProviderExceptionDto> createProviderException(@RequestBody ProviderExceptionDto exception){
        ProviderExceptionDto savedException = availabilityService.createProviderException(exception);
        return ResponseEntity.ok(savedException);
    }

    @PostMapping("/status")
    public ResponseEntity<AvailabilityStatusResponse> checkAvailability(@RequestBody AvailabilityStatusRequest request){
        AvailabilityStatusResponse response = availabilityService.checkAvailability(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<AvailabilityRulesDto> updateAvailabilityRule(@PathVariable Long ruleId,
                                                                      @RequestBody AvailabilityRulesDto rule) {
        AvailabilityRulesDto updatedRule = availabilityService.updateRules(ruleId, rule);
        return ResponseEntity.ok(updatedRule);
    }

    @PutMapping("/exceptions/{exceptionId}")
    public ResponseEntity<ProviderExceptionDto> updateProviderException(@PathVariable Long exceptionId,
                                                                        @RequestBody ProviderExceptionDto exception) {
        ProviderExceptionDto updatedException = availabilityService.updateException(exceptionId, exception);
        return ResponseEntity.ok(updatedException);
    }

    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Void> deleteAvailabilityRule(@PathVariable Long ruleId) {
        availabilityService.deleteAvailabilityRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/exceptions/{exceptionId}")
    public ResponseEntity<Void> deleteProviderException(@PathVariable Long exceptionId) {
        availabilityService.deleteProviderException(exceptionId);
        return ResponseEntity.noContent().build();
    }


}
