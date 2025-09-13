package com.sarthak.BookingService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityStatusResponse {

    private Long serviceProviderId;
    private String startTime;
    private String endTime;
    private String date;
    private String status;
}

