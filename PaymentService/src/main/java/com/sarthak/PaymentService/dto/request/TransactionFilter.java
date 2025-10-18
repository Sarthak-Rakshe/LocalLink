package com.sarthak.PaymentService.dto.request;

public record TransactionFilter(
        String paymentStatus,
        String paymentMethod
) {}
