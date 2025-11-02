package com.sarthak.ReviewService.client;

import com.sarthak.ReviewService.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "Booking-service")
public interface BookingClient {

    @GetMapping("/api/bookings/{serviceId}/{customerId}/bookings")
    public BookingDto getBookingByServiceAndCustomer(
            @PathVariable Long serviceId,
            @PathVariable Long customerId
    );

}
