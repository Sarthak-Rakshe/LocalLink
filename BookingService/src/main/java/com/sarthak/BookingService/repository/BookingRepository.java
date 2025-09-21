package com.sarthak.BookingService.repository;

import com.sarthak.BookingService.dto.BookingStatusCount;
import com.sarthak.BookingService.model.Booking;
import com.sarthak.BookingService.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);

    Page<Booking> findByServiceProviderId(Long serviceProviderId, Pageable pageable);

    Optional<Booking> findByServiceProviderIdAndServiceIdAndBookingDateAndBookingStartTimeAndBookingEndTime(Long serviceProviderId,
                                                                                                            Long serviceId,
                                                                                                            LocalDate bookingDate,
                                                                                                            LocalTime bookingStartTime,
                                                                                                            LocalTime bookingEndTime);

    List<Booking> findAllByServiceProviderIdAndBookingDateOrderByBookingStartTime(Long serviceProviderId, LocalDate date);

    List<Booking> findAllByServiceProviderIdAndServiceIdAndBookingDateOrderByBookingStartTime(Long serviceProviderId, Long serviceId, LocalDate date);

    @Query(value = """
            SELECT new com.sarthak.BookingService.dto.BookingStatusCount(b.bookingStatus, COUNT(b))
            FROM Booking b
            WHERE b.serviceProviderId = :serviceProviderId
            GROUP BY b.bookingStatus
            """)
    List<BookingStatusCount> countBookingsByStatusGrouped(
            @Param("serviceProviderId") Long serviceProviderId
    );

    List<Booking> findAllByBookingStatusAndCreatedAtBefore(BookingStatus bookingStatus, LocalDateTime cutOff);
}
