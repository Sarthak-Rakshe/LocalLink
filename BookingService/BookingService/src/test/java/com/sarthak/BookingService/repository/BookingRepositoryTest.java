package com.sarthak.BookingService.repository;

import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    private Booking buildNew() {
        Booking b = new Booking();
        b.setCustomerId(1L);
        b.setServiceId(2L);
        b.setServiceProviderId(3L);
        b.setServiceCategory("Plumbing");
        b.setBookingStatus(BookingStatus.PENDING);
        b.setBookingTime(LocalDateTime.now()); // set explicitly since @CreatedDate auditing not enabled in test
        return b;
    }

    @Test
    @DisplayName("save and findById works")
    void saveAndFind() {
        Booking saved = bookingRepository.save(buildNew());
        assertNotNull(saved.getBookingId());
        Booking found = bookingRepository.findById(saved.getBookingId()).orElseThrow();
        assertEquals("Plumbing", found.getServiceCategory());
    }

    @Test
    @DisplayName("findAll returns multiple")
    void findAllMultiple() {
        bookingRepository.save(buildNew());
        bookingRepository.save(buildNew());
        List<Booking> all = bookingRepository.findAll();
        assertTrue(all.size() >= 2);
    }
}

