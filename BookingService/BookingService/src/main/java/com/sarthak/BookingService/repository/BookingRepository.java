package com.sarthak.BookingService.repository;

import com.sarthak.BookingService.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<List<Booking>> findByCustomerId(Long customerId);

    Optional<List<Booking>> findByServiceProviderId(Long serviceProviderId);
}
