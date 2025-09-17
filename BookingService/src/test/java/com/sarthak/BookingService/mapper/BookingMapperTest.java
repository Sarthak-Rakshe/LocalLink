package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private final BookingMapper mapper = new BookingMapper();

    private Booking buildEntity() {
        Booking b = new Booking();
        b.setBookingId(5L);
        b.setCustomerId(11L);
        b.setServiceId(22L);
        b.setServiceProviderId(33L);
        b.setServiceCategory("Plumbing");
        b.setBookingStatus(BookingStatus.CONFIRMED);
        b.setBookingDate(LocalDate.parse("2024-01-01"));
        b.setBookingStartTime(LocalTime.parse("13:00"));
        b.setBookingEndTime(LocalTime.parse("14:00"));
        b.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        b.setRescheduledToId("N/A");
        return b;
    }

    @Test
    @DisplayName("toDto maps key fields including date and time-only strings")
    void toDto() {
        Booking entity = buildEntity();
        BookingDto dto = mapper.toDto(entity);
        assertEquals(5L, dto.bookingId());
        assertEquals(11L, dto.customerId());
        assertEquals(22L, dto.serviceId());
        assertEquals(33L, dto.serviceProviderId());
        assertEquals(BookingStatus.CONFIRMED, dto.bookingStatus());
        assertEquals("2024-01-01", dto.bookingDate());
        assertEquals("13:00", dto.bookingStartTime());
        assertEquals("14:00", dto.bookingEndTime());
    }

    @Test
    @DisplayName("toEntity parses date and time-only strings into typed fields")
    void toEntity() {
        BookingDto dto = BookingDto.builder()
                .customerId(11L)
                .serviceId(22L)
                .serviceProviderId(33L)
                .bookingDate("2024-01-01")
                .bookingStartTime("13:00")
                .bookingEndTime("14:00")
                .bookingStatus(BookingStatus.PENDING)
                .build();
        Booking entity = mapper.toEntity(dto);
        assertNull(entity.getBookingId());
        assertEquals(11L, entity.getCustomerId());
        assertEquals(22L, entity.getServiceId());
        assertEquals(33L, entity.getServiceProviderId());
        assertNull(entity.getServiceCategory());
        assertEquals(BookingStatus.PENDING, entity.getBookingStatus());
        assertEquals(LocalDate.parse("2024-01-01"), entity.getBookingDate());
        assertEquals(LocalTime.parse("13:00"), entity.getBookingStartTime());
        assertEquals(LocalTime.parse("14:00"), entity.getBookingEndTime());
    }
}
