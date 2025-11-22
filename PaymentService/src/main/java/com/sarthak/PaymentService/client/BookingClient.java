package com.sarthak.PaymentService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "Booking-Service", url = "${feign.client.config.booking-service.url}")
public interface BookingClient {

    @PostMapping("/api/bookings/{bookingId}/updateStatus/{status}")
    public void updateBookingStatus(@PathVariable("bookingId") Long bookingId,
                                    @PathVariable("status") String status);

}
