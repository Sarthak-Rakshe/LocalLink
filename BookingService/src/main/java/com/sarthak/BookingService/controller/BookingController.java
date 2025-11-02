package com.sarthak.BookingService.controller;

import com.sarthak.BookingService.config.shared.UserPrincipal;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.request.BookingRescheduleRequest;
import com.sarthak.BookingService.dto.response.BookedSlotsResponse;
import com.sarthak.BookingService.dto.response.BookingsSummaryResponse;
import com.sarthak.BookingService.dto.response.PageResponse;
import com.sarthak.BookingService.service.BookingService;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public PageResponse<BookingDto> getAllBookings(@NotNull @RequestParam(name ="sort-by") String sortBy,
                                                   @NotNull @RequestParam(name ="sort-dir") String sortDir,
                                                   @NotNull @RequestParam(name ="page") int page,
                                                   @NotNull @RequestParam(name ="size") int size) {
        Page<BookingDto> bookings = bookingService.getAllBookings(page, size, sortBy, sortDir);
        return new PageResponse<>(
                bookings.getContent(),
                bookings.getNumber(),
                bookings.getTotalElements(),
                bookings.getTotalPages(),
                bookings.getSize()
        );
    }

    @GetMapping("/serviceProvider/{serviceProviderId}/by-date")
    public ResponseEntity<List<BookingDto>> getBookingsByServiceProviderIdAndDate(@PathVariable Long serviceProviderId,
                                                                           @RequestParam LocalDate date) {
        return ResponseEntity.ok(bookingService.getAllByServiceProviderIdAndDate(serviceProviderId, date));
    }

    @GetMapping("/serviceProvider/{serviceProviderId}")
    public PageResponse<BookingDto> getBookingsByServiceProviderId(
            @PathVariable Long serviceProviderId,
            @NotNull @RequestParam(name ="sort-by") String sortBy,
            @NotNull @RequestParam(name ="sort-dir") String sortDir,
            @NotNull @RequestParam(name ="page") int page,
            @NotNull @RequestParam(name ="size") int size
    ) {
        Page<BookingDto> booking = bookingService.getAllByServiceProviderId(serviceProviderId, page, size, sortBy, sortDir);
        return new PageResponse<>(
                booking.getContent(),
                booking.getNumber(),
                booking.getTotalElements(),
                booking.getTotalPages(),
                booking.getSize()
        );
    }

    @GetMapping("/customer/{customerId}")
    public PageResponse<BookingDto> getBookingsByCustomerId(
            @PathVariable Long customerId,
            @NotNull @RequestParam(name ="sort-by") String sortBy,
            @NotNull @RequestParam(name ="sort-dir") String sortDir,
            @NotNull @RequestParam(name ="page") int page,
            @NotNull @RequestParam(name ="size") int size
    ) {
        Page<BookingDto> booking = bookingService.getAllByCustomerId(customerId, page, size, sortBy, sortDir);
        return new PageResponse<>(
                booking.getContent(),
                booking.getNumber(),
                booking.getTotalElements(),
                booking.getTotalPages(),
                booking.getSize()
        );
    }

    @GetMapping("/{serviceId}/{customerId}/bookings")
    public ResponseEntity<BookingDto> getBookingByServiceAndCustomer(
            @PathVariable Long serviceId,
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(bookingService.getBookingByServiceIdAndCustomerId(serviceId, customerId));
    }

    @GetMapping("/my-summary")
    public ResponseEntity<BookingsSummaryResponse> getBookingSummary(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getBookingSummary(authentication));
    }
    
    @GetMapping("bookedSlots/{serviceProviderId}/{serviceId}")
    public ResponseEntity<BookedSlotsResponse> getBookedSlotsForServiceProviderAndService(
            @PathVariable Long serviceProviderId,
            @PathVariable Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.getBookedSlotsForProviderOnDate(serviceProviderId, serviceId, date));
    }

    @PostMapping()
    public ResponseEntity<BookingDto> bookService(@RequestBody BookingDto bookingDto, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookService(bookingDto, userPrincipal));
    }

   @PostMapping("/{bookingId}/updateStatus/{status}")
   public ResponseEntity<BookingDto> updateBookingStatus(@PathVariable("bookingId") Long bookingId,
                                                       @PathVariable("status") String status) {
       return ResponseEntity.ok(bookingService.updateBookingStatus(bookingId, status));
   }

    @PostMapping("/{bookingId}/reschedule")
    public ResponseEntity<BookingDto> rescheduleBooking(@PathVariable("bookingId") Long bookingId,
                                                        @RequestBody BookingRescheduleRequest request) {
        return ResponseEntity.ok(bookingService.rescheduleBooking(bookingId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.updateBookingStatus(id,"DELETED");
        return ResponseEntity.noContent().build();
    }
}
