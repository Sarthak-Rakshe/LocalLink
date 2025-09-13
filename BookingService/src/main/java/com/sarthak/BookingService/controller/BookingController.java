package com.sarthak.BookingService.controller;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingDetails(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingDetails(id));
    }

    @GetMapping()
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/service-provider/{serviceProviderId}")
    public ResponseEntity<List<BookingDto>> getBookingsByServiceProviderId(@PathVariable Long serviceProviderId,
                                                                           @RequestParam @DateTimeFormat(iso =
                                                                                   DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getAllByServiceProviderIdAndDate(serviceProviderId, date));
    }

    @PostMapping()
    public ResponseEntity<BookingDto> bookService(@RequestBody BookingDto bookingDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookService(bookingDto));
    }
}
