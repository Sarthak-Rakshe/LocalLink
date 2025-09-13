package com.sarthak.AvailabilityService.controller;

import com.sarthak.AvailabilityService.dto.AvailabilityRulesDto;
import com.sarthak.AvailabilityService.dto.ProviderExceptionDto;
import com.sarthak.AvailabilityService.dto.request.AvailabilityStatusRequest;
import com.sarthak.AvailabilityService.dto.response.AvailabilityStatusResponse;
import com.sarthak.AvailabilityService.service.AvailabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/rules")
    public ResponseEntity<AvailabilityRulesDto> createAvailabilityRule(@RequestBody AvailabilityRulesDto rule){
        AvailabilityRulesDto savedRule = availabilityService.addAvailabilityRule(rule);
        return ResponseEntity.ok(savedRule);
    }

    @PostMapping("/exceptions")
    public ResponseEntity<ProviderExceptionDto> createProviderException(@RequestBody ProviderExceptionDto exception){
        ProviderExceptionDto savedException = availabilityService.addProviderException(exception);
        return ResponseEntity.ok(savedException);
    }

    @PostMapping("/status")
    public ResponseEntity<AvailabilityStatusResponse> checkAvailability(@RequestBody AvailabilityStatusRequest request){
        AvailabilityStatusResponse response = availabilityService.checkAvailability(request);
        return ResponseEntity.ok(response);
    }

}
