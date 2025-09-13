package com.sarthak.BookingService.repository;

import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    private Booking buildNew(LocalDate date, Instant start, Instant end) {
        Booking b = new Booking();
        b.setCustomerId(1L);
        b.setServiceId(2L);
        b.setServiceProviderId(3L);
        b.setServiceCategory("Plumbing");
        b.setBookingStatus(BookingStatus.PENDING);
        b.setBookingDate(date);
        b.setBookingStartTime(start);
        b.setBookingEndTime(end);
        return b;
    }

    @Test
    @DisplayName("save and findById works")
    void saveAndFind() {
        LocalDate date = LocalDate.parse("2024-01-01");
        Booking saved = bookingRepository.save(buildNew(date,
                Instant.parse("2024-01-01T13:00:00Z"),
                Instant.parse("2024-01-01T14:00:00Z")));
        assertNotNull(saved.getBookingId());
        Booking found = bookingRepository.findById(saved.getBookingId()).orElseThrow();
        assertEquals("Plumbing", found.getServiceCategory());
        assertEquals(date, found.getBookingDate());
    }

    @Test
    @DisplayName("findAll returns multiple and provider+date query works")
    void findAllMultipleAndQuery() {
        LocalDate d1 = LocalDate.parse("2024-01-01");
        LocalDate d2 = LocalDate.parse("2024-01-02");
        bookingRepository.save(buildNew(d1, Instant.parse("2024-01-01T13:00:00Z"), Instant.parse("2024-01-01T14:00:00Z")));
        bookingRepository.save(buildNew(d1, Instant.parse("2024-01-01T15:00:00Z"), Instant.parse("2024-01-01T16:00:00Z")));
        bookingRepository.save(buildNew(d2, Instant.parse("2024-01-02T13:00:00Z"), Instant.parse("2024-01-02T14:00:00Z")));
        List<Booking> all = bookingRepository.findAll();
        assertTrue(all.size() >= 3);
        List<Booking> providerOnD1 = bookingRepository.findAllByServiceProviderIdAndBookingDate(3L, d1);
        assertEquals(2, providerOnD1.size());
    }
}
