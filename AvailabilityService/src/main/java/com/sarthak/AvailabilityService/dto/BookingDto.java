package com.sarthak.AvailabilityService.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookingDto {
    private Long bookingId;
    private Long customerId;
    private Long serviceId;
    private Long serviceProviderId;
    private String serviceCategory;
    private String bookingDate;
    private String bookingStartTime;
    private String bookingEndTime;
    private String bookingStatus;
}
