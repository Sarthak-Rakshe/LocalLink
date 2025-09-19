package com.sarthak.PaymentService.dto;

import lombok.Builder;

@Builder
public record OrderCreateResponse(
        String orderId,
        String status,
        String link
) {}
