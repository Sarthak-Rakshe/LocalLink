package com.sarthak.ReviewService.client;

import com.sarthak.ReviewService.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "Booking-service", url = "${feign.client.config.booking-service.url}")
public interface BookingClient {

    @GetMapping("/api/bookings/{id}")
    public BookingDto getBookingDetails(@PathVariable("id") Long id);

}
