package com.sarthak.BookingService.service;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.exception.BookingNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setup() {
        bookingMapper = new BookingMapper();
        bookingService = new BookingService(bookingRepository, bookingMapper);
    }

    private Booking buildEntity(Long id) {
        Booking b = new Booking();
        b.setBookingId(id);
        b.setCustomerId(11L);
        b.setServiceId(22L);
        b.setServiceProviderId(33L);
        b.setServiceCategory("Plumbing");
        b.setBookingStatus(BookingStatus.PENDING);
        b.setBookingTime(LocalDateTime.now());
        return b;
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
    @DisplayName("bookService returns placeholder string")
    void bookService_placeholder() {
        assertEquals("Under Development", bookingService.bookService());
    }
}

