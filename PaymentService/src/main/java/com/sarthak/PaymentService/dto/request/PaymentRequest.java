package com.sarthak.PaymentService.dto.request;

import lombok.Builder;

@Builder
public record PaymentRequest (
        String orderId,
        Long bookingId,
        Long serviceProviderId,
        Long customerId,
        Double amount,
        String paymentMethod
){}
