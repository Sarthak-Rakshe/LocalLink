package com.sarthak.AvailabilityService.model;

import com.sarthak.AvailabilityService.exception.InvalidAvailableTimeSlotParametersException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "availability")
public class Availability {

    private static final ZoneId BUSINESS_ZONE = ZoneOffset.UTC;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availabilityId;

    @NotNull
    private Long serviceProviderId;

    @NotNull
    private LocalDate availabilityDate;

    @NotNull
    private Instant startTime;

    @NotNull
    private  Instant endTime;


    @PrePersist
    @PreUpdate
    private void validateAvailability(){
        startTime = startTime.truncatedTo(ChronoUnit.SECONDS);
        endTime   = endTime.truncatedTo(ChronoUnit.SECONDS);

        if(!endTime.isAfter(startTime) ){
            throw new InvalidAvailableTimeSlotParametersException("End time must be after start time");
        }

        LocalDate startDate = LocalDateTime.ofInstant(startTime, BUSINESS_ZONE).toLocalDate();
        LocalDate endDate   = LocalDateTime.ofInstant(endTime, BUSINESS_ZONE).toLocalDate();

        if (!(availabilityDate.equals(startDate) && availabilityDate.equals(endDate))) {
            throw new InvalidAvailableTimeSlotParametersException("startTime and endTime must be on availabilityDate");
        }
    }

    public LocalTime getStartLocalTime() {
        return LocalDateTime.ofInstant(startTime, BUSINESS_ZONE).toLocalTime();
    }
    public LocalTime getEndLocalTime() {
        return LocalDateTime.ofInstant(endTime, BUSINESS_ZONE).toLocalTime();
    }

    public void setAndNormalizeStartTime(Instant startTime){
        this.startTime = startTime.truncatedTo(ChronoUnit.SECONDS);
    }
    public void setAndNormalizeEndTime(Instant endTime){
        this.endTime = endTime.truncatedTo(ChronoUnit.SECONDS);
    }

}
