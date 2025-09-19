package com.sarthak.BookingService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "booking", uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_provider_service_booking_time", columnNames = {"service_provider_id", "service_id", "booking_date", "booking_start_time", "booking_end_time"})
}, indexes = {
        @Index(name = "idx_customer_id", columnList = "customer_id"),
        @Index(name = "idx_service_provider_id", columnList = "service_provider_id"),
        @Index(name = "idx_service_id", columnList = "service_id"),
        @Index(name = "idx_booking_date", columnList = "booking_date"),
        @Index(name = "idx_service_provider_booking_status", columnList = "service_provider_id, booking_status")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @NotNull
    @Column(name = "service_id")
    private Long serviceId; // ID of the service being booked

    @NotNull
    @Column(name = "service_provider_id")
    private Long serviceProviderId;

    @NotNull
    @Column(name = "service_category")
    private String serviceCategory;

    @NotNull
    @Column(name = "customer_id")
    private Long customerId; // User ID of the customer

    @NotNull
    @Column(name = "booking_date")
    private LocalDate bookingDate; // Date of the booking

    @NotNull
    @Column(name = "booking_start_time")
    private LocalTime bookingStartTime; // Start time of the booked service

    @NotNull
    @Column(name = "booking_end_time")
    private LocalTime bookingEndTime;   // End time of the booked service

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus; // Status of the booking (e.g., PENDING, CONFIRMED, CANCELLED)

    @NotNull
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt; // Timestamp when the booking was created

    @NotNull
    @Column(name = "rescheduled_to_id")
    private String rescheduledToId; // If rescheduled, the ID of the new booking


    @PrePersist
    @PreUpdate
    public void normalizeBookingTime(){
        if (bookingStartTime == null || bookingEndTime == null) {
            throw new IllegalArgumentException("Booking start time and end time cannot be null");
        }
        if(rescheduledToId == null || rescheduledToId.isBlank()){
            this.rescheduledToId = "N/A";
        }

        this.bookingStartTime = this.bookingStartTime.truncatedTo(ChronoUnit.SECONDS);
        this.bookingEndTime = this.bookingEndTime.truncatedTo(ChronoUnit.SECONDS);
        this.createdAt = this.createdAt == null ? Instant.now().truncatedTo(ChronoUnit.SECONDS) : this.createdAt.truncatedTo(ChronoUnit.SECONDS);
    }
}


