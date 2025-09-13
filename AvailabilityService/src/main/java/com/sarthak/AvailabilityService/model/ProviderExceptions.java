package com.sarthak.AvailabilityService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "provider_exceptions",
    uniqueConstraints = @UniqueConstraint(name = "uk_exception_per_day_per_provider",
    columnNames = {"service_provider_id", "exception_date","new_start_time","new_end_time"}),
    indexes = {@Index(name = "idx_service_provider_id", columnList = "service_provider_id, exception_date")}
)
public class ProviderExceptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exceptionId;

    @NotNull
    @Column(name = "service_provider_id")
    private Long serviceProviderId;

    @NotNull
    @Column(name = "exception_date")
    private LocalDate exceptionDate;

    @NotNull
    @Column(name = "new_start_time")
    private LocalTime newStartTime;

    @NotNull
    @Column(name = "new_end_time")
    private LocalTime newEndTime;

    @NotNull
    @Column(name = "exception_reason")
    private String exceptionReason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type")
    private ExceptionType exceptionType;
}
