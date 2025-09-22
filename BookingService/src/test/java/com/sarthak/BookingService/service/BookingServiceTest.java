package com.sarthak.BookingService.service;

import com.sarthak.BookingService.client.AvailabilityServiceClient;
import com.sarthak.BookingService.dto.AvailabilityStatus;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.dto.BookingStatusCount;
import com.sarthak.BookingService.dto.request.BookingRescheduleRequest;
import com.sarthak.BookingService.dto.response.AvailabilityStatusResponse;
import com.sarthak.BookingService.exception.BookingNotFoundException;
import com.sarthak.BookingService.exception.ProviderNotAvailableForGivenTimeSlotException;
import com.sarthak.BookingService.exception.TimeSlotAlreadyBookedException;
import com.sarthak.BookingService.mapper.BookingMapper;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import com.sarthak.BookingService.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingMapper bookingMapper;

    @Mock
    private AvailabilityServiceClient availabilityServiceClient;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setup() {
        bookingMapper = new BookingMapper();
        bookingService = new BookingService(bookingRepository, bookingMapper, availabilityServiceClient);
    }

    private Booking buildEntity(Long id) {
        return Booking.builder()
                .bookingId(id)
                .customerId(11L)
                .serviceId(22L)
                .serviceProviderId(33L)
                .serviceCategory("Plumbing")
                .bookingStatus(BookingStatus.PENDING)
                .bookingDate(LocalDate.parse("2024-01-01"))
                .bookingStartTime(LocalTime.parse("13:00"))
                .bookingEndTime(LocalTime.parse("14:00"))
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .rescheduledToId("N/A")
                .build();
    }

    private BookingDto buildDto() {
        return BookingDto.builder()
                .customerId(11L)
                .serviceId(22L)
                .serviceProviderId(33L)
                .bookingDate("2024-01-01")
                .bookingStartTime("13:00")
                .bookingEndTime("14:00")
                .bookingStatus(BookingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("getBookingDetails returns dto when found")
    void getBookingDetails_found() {
        Booking entity = buildEntity(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(entity));
        BookingDto dto = bookingService.getBookingDetails(1L);
        assertEquals(1L, dto.bookingId());
        assertEquals("2024-01-01", dto.bookingDate());
        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("getBookingDetails throws when missing")
    void getBookingDetails_notFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.getBookingDetails(99L));
    }

    @Test
    @DisplayName("getAllBookings returns page mapped to dto")
    void getAllBookings_paged() {
        Booking e1 = buildEntity(1L);
        Booking e2 = buildEntity(2L);
        Page<Booking> page = new PageImpl<>(List.of(e1, e2));
        when(bookingRepository.findAll(PageRequest.of(0, 2))).thenReturn(page);

        Page<BookingDto> result = bookingService.getAllBookings(0, 2);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).bookingId());
        verify(bookingRepository).findAll(PageRequest.of(0, 2));
    }

    @Test
    @DisplayName("getAllByCustomerId returns page mapped to dto")
    void getAllByCustomerId_paged() {
        Page<Booking> page = new PageImpl<>(List.of(buildEntity(1L)));
        when(bookingRepository.findByCustomerId(eq(11L), eq(PageRequest.of(0, 1)))).thenReturn(page);

        Page<BookingDto> result = bookingService.getAllByCustomerId(11L, 0, 1);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).bookingId());
    }

    @Test
    @DisplayName("getAllByServiceProviderId returns page mapped to dto")
    void getAllByServiceProviderId_paged() {
        Page<Booking> page = new PageImpl<>(List.of(buildEntity(3L)));
        when(bookingRepository.findByServiceProviderId(eq(33L), eq(PageRequest.of(0, 1)))).thenReturn(page);

        Page<BookingDto> result = bookingService.getAllByServiceProviderId(33L, 0, 1);
        assertEquals(1, result.getTotalElements());
        assertEquals(3L, result.getContent().get(0).bookingId());
    }

    @Test
    @DisplayName("getAllByServiceProviderIdAndDate returns list mapped to dto")
    void getAllByServiceProviderIdAndDate_list() {
        when(bookingRepository.findAllByServiceProviderIdAndBookingDateOrderByBookingStartTime(eq(33L), eq(LocalDate.parse("2024-01-01"))))
                .thenReturn(List.of(buildEntity(10L)));
        var list = bookingService.getAllByServiceProviderIdAndDate(33L, LocalDate.parse("2024-01-01"));
        assertEquals(1, list.size());
        assertEquals(10L, list.get(0).bookingId());
    }

    @Test
    @DisplayName("bookService saves when AVAILABLE and no exact conflict; status PENDING")
    void bookService_success() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", AvailabilityStatus.AVAILABLE)
        );
        when(bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                eq(33L), eq(22L), eq(LocalDate.parse("2024-01-01")), eq(LocalTime.parse("13:00")), eq(LocalTime.parse("14:00"))
        )).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking toSave = invocation.getArgument(0);
            toSave.setBookingId(123L);
            toSave.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
            if (toSave.getRescheduledToId() == null || toSave.getRescheduledToId().isBlank()) {
                toSave.setRescheduledToId("N/A");
            }
            return toSave;
        });

        BookingDto result = bookingService.bookService(input);
        assertNotNull(result.bookingId());
        assertEquals(BookingStatus.PENDING, result.bookingStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("bookService throws when provider BLOCKED or OUTSIDE_WORKING_HOURS")
    void bookService_blocked() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", AvailabilityStatus.BLOCKED)
        );
        assertThrows(ProviderNotAvailableForGivenTimeSlotException.class, () -> bookingService.bookService(input));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("bookService throws when exact conflicting booking exists")
    void bookService_conflict() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", AvailabilityStatus.AVAILABLE)
        );
        Booking existing = buildEntity(77L);
        when(bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                eq(33L), eq(22L), eq(LocalDate.parse("2024-01-01")), eq(LocalTime.parse("13:00")), eq(LocalTime.parse("14:00"))
        )).thenReturn(Optional.of(existing));
        assertThrows(TimeSlotAlreadyBookedException.class, () -> bookingService.bookService(input));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelBooking sets status CANCELLED")
    void cancelBooking() {
        Booking existing = buildEntity(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        BookingDto dto = bookingService.cancelBooking(5L);
        assertEquals(BookingStatus.CANCELLED, dto.bookingStatus());
    }

    @Test
    @DisplayName("completeBooking sets status COMPLETED")
    void completeBooking() {
        Booking existing = buildEntity(6L);
        when(bookingRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        BookingDto dto = bookingService.completeBooking(6L);
        assertEquals(BookingStatus.COMPLETED, dto.bookingStatus());
    }

    @Test
    @DisplayName("confirmBooking sets status CONFIRMED")
    void confirmBooking() {
        Booking existing = buildEntity(7L);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        BookingDto dto = bookingService.confirmBooking(7L);
        assertEquals(BookingStatus.CONFIRMED, dto.bookingStatus());
    }

    @Test
    @DisplayName("deleteBooking sets status DELETED (soft delete)")
    void deleteBooking() {
        Booking existing = buildEntity(8L);
        when(bookingRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        bookingService.deleteBooking(8L);
        assertEquals(BookingStatus.DELETED, existing.getBookingStatus());
        verify(bookingRepository).save(existing);
    }

    @Test
    @DisplayName("rescheduleBooking creates new PENDING booking and marks old as RESCHEDULED")
    void rescheduleBooking_success() {
        Booking existing = buildEntity(9L);
        when(bookingRepository.findById(9L)).thenReturn(Optional.of(existing));

        // Availability says slot is available
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "15:00", "16:00", "2024-01-02", AvailabilityStatus.AVAILABLE)
        );
        // No conflicting booking
        when(bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                eq(33L), eq(22L), eq(LocalDate.parse("2024-01-02")), eq(LocalTime.parse("15:00")), eq(LocalTime.parse("16:00"))
        )).thenReturn(Optional.empty());

        // First and second save stubbing: first call (new booking) assigns id; second call (existing booking update) returns as-is
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(inv -> {
                    Booking saved = inv.getArgument(0);
                    if (saved != null && saved.getBookingId() == null) {
                        saved.setBookingId(200L);
                        saved.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
                        if (saved.getRescheduledToId() == null || saved.getRescheduledToId().isBlank()) {
                            saved.setRescheduledToId("N/A");
                        }
                    }
                    return saved;
                });

        BookingRescheduleRequest req = new BookingRescheduleRequest(
                9L,
                LocalDate.parse("2024-01-02"),
                LocalTime.parse("15:00"),
                LocalTime.parse("16:00")
        );

        BookingDto newDto = bookingService.rescheduleBooking(9L, req);
        assertEquals(200L, newDto.bookingId());
        assertEquals(BookingStatus.PENDING, newDto.bookingStatus());
        assertEquals("2024-01-02", newDto.bookingDate());
        assertEquals("15:00", newDto.bookingStartTime());
        assertEquals("16:00", newDto.bookingEndTime());
        assertEquals(BookingStatus.RESCHEDULED, existing.getBookingStatus());
        assertEquals("200", existing.getRescheduledToId());
    }

    @Test
    @DisplayName("rescheduleBooking throws when new slot is not available")
    void rescheduleBooking_blocked() {
        Booking existing = buildEntity(10L);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "15:00", "16:00", "2024-01-02", AvailabilityStatus.BLOCKED)
        );

        BookingRescheduleRequest req = new BookingRescheduleRequest(
                10L,
                LocalDate.parse("2024-01-02"), LocalTime.parse("15:00"), LocalTime.parse("16:00")
        );
        assertThrows(TimeSlotAlreadyBookedException.class, () -> bookingService.rescheduleBooking(10L, req));
    }

    @Test
    @DisplayName("rescheduleBooking throws when exact conflicting booking exists")
    void rescheduleBooking_conflict() {
        Booking existing = buildEntity(11L);
        when(bookingRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", AvailabilityStatus.AVAILABLE)
        );
        // Conflict with another booking (different id)
        when(bookingRepository.findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(
                eq(33L), eq(22L), eq(LocalDate.parse("2024-01-01")), eq(LocalTime.parse("13:00")), eq(LocalTime.parse("14:00"))
        )).thenReturn(Optional.of(buildEntity(999L)));

        BookingRescheduleRequest req = new BookingRescheduleRequest(null, null, null, null); // keep same time
        assertThrows(TimeSlotAlreadyBookedException.class, () -> bookingService.rescheduleBooking(11L, req));
    }

    @Test
    @DisplayName("getBookingSummaryForServiceProvider aggregates counts and computes totals")
    void getBookingSummaryForServiceProvider_counts() {
        when(bookingRepository.countBookingsByStatusGrouped(33L)).thenReturn(
                List.of(
                        new BookingStatusCount(BookingStatus.COMPLETED, 2L),
                        new BookingStatusCount(BookingStatus.PENDING, 3L),
                        new BookingStatusCount(BookingStatus.CANCELLED, 1L),
                        new BookingStatusCount(BookingStatus.RESCHEDULED, 5L),
                        new BookingStatusCount(BookingStatus.DELETED, 4L)
                )
        );

        var summary = bookingService.getBookingSummaryForServiceProvider(33L);
        assertEquals(2L, summary.completedBookings());
        assertEquals(3L, summary.pendingBookings());
        assertEquals(1L, summary.cancelledBookings());
        assertEquals(5L, summary.rescheduledBookings());
        assertEquals(4L, summary.deletedBookings());
        assertEquals(2L + 3L + 1L, summary.totalBookings());
    }

    @Test
    @DisplayName("getBookingSummaryForServiceProvider returns zeros when no data")
    void getBookingSummaryForServiceProvider_empty() {
        when(bookingRepository.countBookingsByStatusGrouped(99L)).thenReturn(List.of());
        var summary = bookingService.getBookingSummaryForServiceProvider(99L);
        assertEquals(0L, summary.completedBookings());
        assertEquals(0L, summary.pendingBookings());
        assertEquals(0L, summary.cancelledBookings());
        assertEquals(0L, summary.rescheduledBookings());
        assertEquals(0L, summary.deletedBookings());
        assertEquals(0L, summary.totalBookings());
    }

}
