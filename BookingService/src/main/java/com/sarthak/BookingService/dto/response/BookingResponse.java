package com.sarthak.BookingService.dto.response;

import com.sarthak.BookingService.dto.CustomerDto;
import com.sarthak.BookingService.dto.ServiceDto;
import com.sarthak.BookingService.dto.ServiceProviderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private CustomerDto customer;
    private ServiceDto service;
    private ServiceProviderDto serviceProvider;
    private String bookingDate;
    private String bookingStartTime;
    private String bookingEndTime;
    private String bookingStatus;
    private String createdAt;
    private Double amount;
    private String rescheduledToId;
}
