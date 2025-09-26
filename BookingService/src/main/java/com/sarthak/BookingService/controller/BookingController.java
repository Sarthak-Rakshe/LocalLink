package com.sarthak.BookingService.controller;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.request.BookingRescheduleRequest;
import com.sarthak.BookingService.dto.response.BookedSlotsResponse;
import com.sarthak.BookingService.dto.response.BookingsSummaryResponse;
import com.sarthak.BookingService.dto.response.PageResponse;
import com.sarthak.BookingService.service.BookingService;
import org.springframework.data.domain.Page;
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
    public PageResponse<BookingDto> getAllBookings(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Page<BookingDto> bookings = bookingService.getAllBookings(page, size);
        return new PageResponse<>(
                bookings.getContent(),
                bookings.getNumber(),
                bookings.getTotalElements(),
                bookings.getTotalPages(),
                bookings.getSize()
        );
    }


    @GetMapping("/serviceProvider/{serviceProviderId}")
    public ResponseEntity<List<BookingDto>> getBookingsByServiceProviderId(@PathVariable Long serviceProviderId,
                                                                           @RequestParam LocalDate date) {
        return ResponseEntity.ok(bookingService.getAllByServiceProviderIdAndDate(serviceProviderId, date));
    }

    @GetMapping("/customer/{customerId}")
    public PageResponse<BookingDto> getBookingsByCustomerId(@PathVariable Long customerId,
                                                                   @RequestParam int page, @RequestParam int size) {
        Page<BookingDto> booking = bookingService.getAllByCustomerId(customerId, page, size);
        return new PageResponse<>(
                booking.getContent(),
                booking.getNumber(),
                booking.getTotalElements(),
                booking.getTotalPages(),
                booking.getSize()
        );
    }

    @GetMapping("summary/{serviceProviderId}")
    public ResponseEntity<BookingsSummaryResponse> getBookingSummaryForProvider(@PathVariable Long serviceProviderId) {
        return ResponseEntity.ok(bookingService.getBookingSummaryForServiceProvider(serviceProviderId));
    }
    
    @GetMapping("bookedSlots/{serviceProviderId}/{serviceId}")
    public ResponseEntity<BookedSlotsResponse> getBookedSlotsForServiceProviderAndService(
            @PathVariable Long serviceProviderId,
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getBookedSlotsForProviderOnDate(serviceProviderId, serviceId, date));
    }

    @PostMapping()
    public ResponseEntity<BookingDto> bookService(@RequestBody BookingDto bookingDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookService(bookingDto));
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingDto> confirmBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }

    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<BookingDto> completeBooking(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(bookingService.completeBooking(bookingId));
    }

    @PostMapping("/{bookingId}/reschedule")
    public ResponseEntity<BookingDto> rescheduleBooking(@PathVariable("bookingId") Long bookingId,
                                                        @RequestBody BookingRescheduleRequest request) {
        return ResponseEntity.ok(bookingService.rescheduleBooking(bookingId, request));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
