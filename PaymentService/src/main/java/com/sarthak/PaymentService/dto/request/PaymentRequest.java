package com.sarthak.PaymentService.dto.request;

import lombok.Builder;

@Builder
public record PaymentRequest (
        String orderId,
        Long bookingId,
        Long customerId,
        Double amount,
        String paymentMethod
){}
