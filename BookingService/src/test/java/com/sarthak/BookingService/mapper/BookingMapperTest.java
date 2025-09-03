package com.sarthak.BookingService.mapper;

import com.sarthak.BookingService.dto.BookingDto;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        b.setBookingTime(LocalDateTime.of(2024,1,1,13,0));
        return b;
    }

    @Test
    @DisplayName("toDto maps all fields and formats time")
    void toDto() {
        Booking entity = buildEntity();
        String expectedFormatted = entity.getBookingTime().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"));
        BookingDto dto = mapper.toDto(entity);
        assertEquals(5L, dto.getBookingId());
        assertEquals(11L, dto.getCustomerId());
        assertEquals(22L, dto.getServiceId());
        assertEquals(33L, dto.getServiceProviderId());
        assertEquals("Plumbing", dto.getServiceCategory());
        assertEquals(BookingStatus.CONFIRMED, dto.getBookingStatus());
        assertEquals(expectedFormatted, dto.getBookingTime());
    }

    @Test
    @DisplayName("toEntity maps basic fields excluding id/time")
    void toEntity() {
        BookingDto dto = new BookingDto();
        dto.setCustomerId(11L);
        dto.setServiceId(22L);
        dto.setServiceProviderId(33L);
        dto.setServiceCategory("Plumbing");
        dto.setBookingStatus(BookingStatus.PENDING);
        Booking entity = mapper.toEntity(dto);
        assertNull(entity.getBookingId());
        assertEquals(11L, entity.getCustomerId());
        assertEquals(22L, entity.getServiceId());
        assertEquals(33L, entity.getServiceProviderId());
        assertEquals("Plumbing", entity.getServiceCategory());
        assertEquals(BookingStatus.PENDING, entity.getBookingStatus());
    }
}

