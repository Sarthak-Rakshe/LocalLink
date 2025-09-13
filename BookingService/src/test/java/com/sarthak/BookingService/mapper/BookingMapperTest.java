package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

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
        b.setBookingStartTime(Instant.parse("2024-01-01T13:00:00Z"));
        b.setBookingEndTime(Instant.parse("2024-01-01T14:00:00Z"));
        return b;
    }

    @Test
    @DisplayName("toDto maps all fields including ISO instants and date")
    void toDto() {
        Booking entity = buildEntity();
        BookingDto dto = mapper.toDto(entity);
        assertEquals(5L, dto.getBookingId());
        assertEquals(11L, dto.getCustomerId());
        assertEquals(22L, dto.getServiceId());
        assertEquals(33L, dto.getServiceProviderId());
        assertEquals("Plumbing", dto.getServiceCategory());
        assertEquals(BookingStatus.CONFIRMED, dto.getBookingStatus());
        assertEquals("2024-01-01", dto.getBookingDate());
        assertEquals("2024-01-01T13:00:00Z", dto.getBookingStartTime());
        assertEquals("2024-01-01T14:00:00Z", dto.getBookingEndTime());
    }

    @Test
    @DisplayName("toEntity parses date and time-only strings into typed fields")
    void toEntity() {
        BookingDto dto = new BookingDto();
        dto.setCustomerId(11L);
        dto.setServiceId(22L);
        dto.setServiceProviderId(33L);
        dto.setServiceCategory("Plumbing");
        dto.setBookingDate("2024-01-01");
        dto.setBookingStartTime("13:00");
        dto.setBookingEndTime("14:00");
        dto.setBookingStatus(BookingStatus.PENDING);
        Booking entity = mapper.toEntity(dto);
        assertNull(entity.getBookingId());
        assertEquals(11L, entity.getCustomerId());
        assertEquals(22L, entity.getServiceId());
        assertEquals(33L, entity.getServiceProviderId());
        assertEquals("Plumbing", entity.getServiceCategory());
        assertEquals(BookingStatus.PENDING, entity.getBookingStatus());
        assertEquals(LocalDate.parse("2024-01-01"), entity.getBookingDate());
        assertEquals(Instant.parse("2024-01-01T13:00:00Z"), entity.getBookingStartTime());
        assertEquals(Instant.parse("2024-01-01T14:00:00Z"), entity.getBookingEndTime());
    }
}
