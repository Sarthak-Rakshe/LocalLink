package com.sarthak.BookingService.client;

import com.sarthak.BookingService.dto.request.AvailabilityStatusRequest;
import com.sarthak.BookingService.dto.response.AvailabilityStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "AvailabilityService")
public interface AvailabilityServiceClient {

    @GetMapping("/api/availability/status")
    public AvailabilityStatusResponse getAvailabilityStatus(@RequestBody AvailabilityStatusRequest request);

}
