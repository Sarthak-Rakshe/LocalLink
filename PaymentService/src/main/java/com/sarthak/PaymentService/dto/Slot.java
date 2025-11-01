package com.sarthak.PaymentService.dto;

import java.time.LocalTime;

public record Slot(
        LocalTime startTime,
        LocalTime endTime
) {}
