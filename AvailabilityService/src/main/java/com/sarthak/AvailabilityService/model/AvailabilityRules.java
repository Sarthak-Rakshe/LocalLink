package com.sarthak.AvailabilityService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "availability_rules",
        uniqueConstraints = @UniqueConstraint(name = "uk_provider_day_start_end",
        columnNames = {"service_provider_id","day_of_week","start_time","end_time"}),
        indexes = {
            @Index(name = "idx_provider_day", columnList = "service_provider_id, day_of_week")
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
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time")
    private LocalTime endTime;

    @NotNull
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

}
