package com.sarthak.PaymentService.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshPaymentStatusRequest(
        @NotBlank(message = "transactionReference must not be blank") String transactionReference
) {}

