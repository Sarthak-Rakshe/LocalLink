package com.sarthak.BookingService.repository;

import com.sarthak.BookingService.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findAllByServiceProviderIdAndBookingDate(Long serviceProviderId, LocalDate bookingDate);
}
