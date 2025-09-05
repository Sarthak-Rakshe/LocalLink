package com.sarthak.BookingService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @NotNull
    private Long serviceId; // ID of the service being booked

    @NotNull
    private Long serviceProviderId;

    @NotNull
    private String serviceCategory;

    @NotNull
    private Long customerId; // User ID of the customer

    @NotNull
    private LocalDate bookingDate; // Date of the booking

    @NotNull
    private Instant bookingStartTime; // Start time of the booked service

    @NotNull
    private Instant bookingEndTime;   // End time of the booked service

    @NotNull
    private BookingStatus bookingStatus; // Status of the booking (e.g., PENDING, CONFIRMED, CANCELLED)

    public void setBookingStartTime(String bookingStartTime){
        this.bookingStartTime = Instant.parse(bookingStartTime);
    }

    public void setBookingEndTime(String bookingEndTime){
        this.bookingEndTime = Instant.parse(bookingEndTime);
    }

    public void setBookingDate(String bookingDate){
        this.bookingDate = LocalDate.parse(bookingDate, DateTimeFormatter.ISO_DATE);
    }


    @PrePersist
    @PreUpdate
    public void normalizeBookingTime(){
        if (bookingStartTime == null || bookingEndTime == null) {
            throw new IllegalArgumentException("Booking start time and end time cannot be null");
        }

        this.bookingStartTime = this.bookingStartTime.truncatedTo(ChronoUnit.SECONDS);
        this.bookingEndTime = this.bookingEndTime.truncatedTo(ChronoUnit.SECONDS);
    }
}


