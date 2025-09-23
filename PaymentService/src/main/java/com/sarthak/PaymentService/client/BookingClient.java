package com.sarthak.PaymentService.client;

import com.sarthak.PaymentService.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "BookingService")
public interface BookingClient {

    @PostMapping("/api/bookings/{bookingId}/confirm")
    BookingDto confirmBooking(@PathVariable("bookingId") Long bookingId);


}
