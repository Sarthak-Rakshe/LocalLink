package com.sarthak.AvailabilityService.model;

import com.sarthak.AvailabilityService.exception.InvalidDayOfWeekException;
import com.sarthak.AvailabilityService.exception.InvalidTimeSlotParametersException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "availability_rules",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_service_start_end",
        columnNames = {"service_provider_id","service_id","start_time","end_time"}),
        indexes = {
            @Index(name = "idx_provider_day", columnList = "service_provider_id, days_of_week"),
            @Index(name = "idx_provide_service_id", columnList = "service_provider_id, service_id"),
            @Index(name = "idx_service_id", columnList = "service_id")
        }
        )
public class AvailabilityRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @NotNull
    @Column(name = "service_provider_id")
    private Long serviceProviderId;

    @NotNull
    @Column(name = "service_id")
    private Long serviceId;

    @NotNull
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time")
    private LocalTime endTime;

    @NotNull
    @Column(name = "days_of_week")
    private byte daysOfWeek;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /*

    BITMASK TABLE FOR daysOfWeek
    ----------------------------------------------
    Day         | Value | Bit Position | Bit Value
    ----------------------------------------------
    Sunday      | 7     | 0            | 00000001
    Monday      | 1     | 1            | 00000010
    Tuesday     | 2     | 2            | 00000100
    Wednesday   | 3     | 3            | 00001000
    Thursday    | 4     | 4            | 00010000
    Friday      | 5     | 5            | 00100000
    Saturday    | 6     | 6            | 01000000
    ----------------------------------------------

    */

    public void setDaysOfWeek(DayOfWeek[] daysOfWeek){
        byte bitmask = 0;
        for(DayOfWeek day: daysOfWeek){
            bitmask |= (byte) (1 << (day.getValue() % 7));
        }
        this.daysOfWeek = bitmask;
    }

    public void addDay(DayOfWeek day){
        this.daysOfWeek |= (byte)(1 << (day.getValue() % 7));
    }

    public void removeDay(DayOfWeek day){
        this.daysOfWeek &= (byte)~(1 << (day.getValue() % 7));
    }

    public void switchAvailability(DayOfWeek day){
        this.daysOfWeek ^= (byte)(1 << (day.getValue() % 7));
    }

    public DayOfWeek[] getDaysOfWeek(){
        DayOfWeek[] days = new DayOfWeek[7];
        int index = 0;
        for(int i=0; i<7; i++){
            if((daysOfWeek & (1 << i)) != 0){
                days[index++] = DayOfWeek.of((i== 0) ? 7: i);
            }
        }
        DayOfWeek[] result = new DayOfWeek[index];
        System.arraycopy(days, 0, result, 0, index);
        return result;
    }

    public boolean isAvailableOn(DayOfWeek day){
        int mask = 1 << (day.getValue() % 7);
        return (daysOfWeek & mask) != 0;
    }

    @PrePersist
    @PreUpdate
    public void onChange() {
        if (this.startTime == null || this.endTime == null) {
            throw new InvalidTimeSlotParametersException("Start time and end time must not be null.");
        }
        if (this.startTime.isAfter(this.endTime) || this.startTime.equals(this.endTime)) {
            throw new InvalidTimeSlotParametersException("Start time must be before end time.");
        }
        if (this.daysOfWeek == 0) {
            throw new InvalidDayOfWeekException("At least one day of the week must be set for availability.");
        }

        this.createdAt = this.createdAt == null ? Instant.now().truncatedTo(ChronoUnit.SECONDS) : this.createdAt.truncatedTo(ChronoUnit.SECONDS);

        this.startTime = startTime.truncatedTo(ChronoUnit.SECONDS);
        this.endTime = endTime.truncatedTo(ChronoUnit.SECONDS);

    }

}
