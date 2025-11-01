package com.sarthak.PaymentService.dto.request;

import com.sarthak.PaymentService.dto.Slot;

public record CreateOrderRequest (
        Long serviceId,
        Slot slot,
        Double pricePerHour
){}
