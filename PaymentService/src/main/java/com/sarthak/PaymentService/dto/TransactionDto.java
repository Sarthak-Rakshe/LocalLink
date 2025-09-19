package com.sarthak.PaymentService.dto;

import lombok.Builder;

@Builder
public record TransactionDto(
        Long transactionId,
        Long bookingId,
        Long customerId,
        Double amount,
        String paymentMethod,
        String paymentStatus,
        String transactionReference,
        String createdAt
){}
