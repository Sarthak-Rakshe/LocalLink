package com.sarthak.BookingService.service;

import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.sarthak.BookingService.model.BookingStatus.CANCELLED;
import static com.sarthak.BookingService.model.BookingStatus.PENDING;

@Service
@Slf4j
public class CleanupService {

    private final BookingRepository bookingRepository;
    private final int SCHEDULE_TIME_IN_SECONDS = 120_000; // 2 minutes

    public CleanupService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }


    @Transactional
    @Scheduled(fixedRate = SCHEDULE_TIME_IN_SECONDS) // Runs every 60 seconds
    public void cancelPendingBookings(){

        log.debug("Scheduled task started: Cancelling pending bookings older than 30 minutes");
        LocalDateTime cutOff = LocalDateTime.now().minusMinutes(30);
        List<Booking> pendingBookings = bookingRepository
                .findAllByBookingStatusAndCreatedAtBefore(PENDING, cutOff);

        pendingBookings.forEach(p -> {
            p.setBookingStatus(CANCELLED);
            bookingRepository.save(p);
            log.debug("Cancelled pending booking with bookingId: {} due to non-confirmation within 30 minutes",
                    p.getBookingId());
        });

        bookingRepository.saveAll(pendingBookings);
        log.debug("Cancelled {} pending bookings older than 30 minutes", pendingBookings.size());

    }

}
