package com.sarthak.AvailabilityService.client;

import com.sarthak.AvailabilityService.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "BookingService")
public interface BookingClient {

    @GetMapping("/api/bookings/service-provider/{serviceProviderId}")
    List<BookingDto> getAllBookingsByServiceProviderIdAndDate(
            @PathVariable("serviceProviderId") Long serviceProviderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
