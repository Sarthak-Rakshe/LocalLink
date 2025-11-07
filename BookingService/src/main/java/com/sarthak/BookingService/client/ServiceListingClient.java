package com.sarthak.BookingService.client;

import com.sarthak.BookingService.dto.ServiceListingQueryFilter;
import com.sarthak.BookingService.dto.ServiceItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "Service-Listing-Service")
public interface ServiceListingClient {

    @GetMapping("/api/services/all-services")
    Page<ServiceItemDto> getServiceDetails(
            @RequestBody (required = false) ServiceListingQueryFilter serviceListingQueryFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    );
}
