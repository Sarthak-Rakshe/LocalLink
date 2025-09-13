package com.sarthak.BookingService.service;

import com.sarthak.BookingService.client.AvailabilityServiceClient;
import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.exception.BookingNotFoundException;
import com.sarthak.BookingService.exception.ProviderNotAvailableForGivenTimeSlotException;
import com.sarthak.BookingService.exception.TimeSlotAlreadyBookedException;
import com.sarthak.BookingService.exception.UnknownAvailabilityStatusException;
import com.sarthak.BookingService.mapper.BookingMapper;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import com.sarthak.BookingService.repository.BookingRepository;
import com.sarthak.BookingService.dto.response.AvailabilityStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
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
        Booking b = new Booking();
        b.setBookingId(id);
        b.setCustomerId(11L);
        b.setServiceId(22L);
        b.setServiceProviderId(33L);
        b.setServiceCategory("Plumbing");
        b.setBookingStatus(BookingStatus.PENDING);
        b.setBookingDate(LocalDate.parse("2024-01-01"));
        b.setBookingStartTime(Instant.parse("2024-01-01T13:00:00Z"));
        b.setBookingEndTime(Instant.parse("2024-01-01T14:00:00Z"));
        return b;
    }

    private BookingDto buildDto() {
        BookingDto dto = new BookingDto();
        dto.setCustomerId(11L);
        dto.setServiceId(22L);
        dto.setServiceProviderId(33L);
        dto.setServiceCategory("Plumbing");
        dto.setBookingDate("2024-01-01");
        dto.setBookingStartTime("13:00");
        dto.setBookingEndTime("14:00");
        dto.setBookingStatus(BookingStatus.PENDING);
        return dto;
    }

    @Test
    @DisplayName("getBookingDetails returns dto when found")
    void getBookingDetails_found() {
        Booking entity = buildEntity(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(entity));
        BookingDto dto = bookingService.getBookingDetails(1L);
        assertEquals(1L, dto.getBookingId());
        assertEquals("Plumbing", dto.getServiceCategory());
        verify(bookingRepository).findById(1L);
    }

    @Test
    @DisplayName("getBookingDetails throws when missing")
    void getBookingDetails_notFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.getBookingDetails(99L));
    }

    @Test
    @DisplayName("getAllBookings maps list")
    void getAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(buildEntity(1L), buildEntity(2L)));
        List<BookingDto> list = bookingService.getAllBookings();
        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getBookingId());
        verify(bookingRepository).findAll();
    }

    @Test
    @DisplayName("bookService confirms and saves when AVAILABLE and no overlap")
    void bookService_success() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", "AVAILABLE")
        );
        when(bookingRepository.findAllByServiceProviderIdAndBookingDate(eq(33L), eq(LocalDate.parse("2024-01-01"))))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking toSave = invocation.getArgument(0);
            toSave.setBookingId(123L);
            return toSave;
        });

        BookingDto result = bookingService.bookService(input);
        assertNotNull(result.getBookingId());
        assertEquals(BookingStatus.CONFIRMED, result.getBookingStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("bookService throws when provider BLOCKED")
    void bookService_blocked() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", "BLOCKED")
        );
        assertThrows(ProviderNotAvailableForGivenTimeSlotException.class, () -> bookingService.bookService(input));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("bookService throws when overlapping with existing booking")
    void bookService_overlap() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", "AVAILABLE")
        );
        Booking existing = buildEntity(77L);
        existing.setBookingStartTime(Instant.parse("2024-01-01T13:30:00Z"));
        existing.setBookingEndTime(Instant.parse("2024-01-01T13:45:00Z"));
        when(bookingRepository.findAllByServiceProviderIdAndBookingDate(eq(33L), eq(LocalDate.parse("2024-01-01"))))
                .thenReturn(List.of(existing));
        assertThrows(TimeSlotAlreadyBookedException.class, () -> bookingService.bookService(input));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("bookService throws for unknown availability status")
    void bookService_unknownStatus() {
        BookingDto input = buildDto();
        when(availabilityServiceClient.getAvailabilityStatus(any())).thenReturn(
                new AvailabilityStatusResponse(33L, "13:00", "14:00", "2024-01-01", "SOMETHING")
        );
        assertThrows(UnknownAvailabilityStatusException.class, () -> bookingService.bookService(input));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelBooking sets status CANCELLED")
    void cancelBooking() {
        Booking existing = buildEntity(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        BookingDto dto = bookingService.cancelBooking(5L);
        assertEquals(BookingStatus.CANCELLED, dto.getBookingStatus());
    }

    @Test
    @DisplayName("completeBooking sets status COMPLETED")
    void completeBooking() {
        Booking existing = buildEntity(6L);
        when(bookingRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        BookingDto dto = bookingService.completeBooking(6L);
        assertEquals(BookingStatus.COMPLETED, dto.getBookingStatus());
    }
}
