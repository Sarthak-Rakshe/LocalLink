package com.sarthak.AvailabilityService.client;

import com.sarthak.AvailabilityService.dto.BookedSlotsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "Booking-Service", url = "${feign.client.config.booking-service.url}")
public interface BookingClient {

    @GetMapping("/api/bookings/bookedSlots/{serviceProviderId}/{serviceId}")
    BookedSlotsResponse getBookedSlotsForProviderOnDate(
            @PathVariable("serviceProviderId") Long serviceProviderId,
            @PathVariable("serviceId") Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
