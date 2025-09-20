package com.sarthak.PaymentService.dto.response;

import lombok.Builder;

@Builder
public record CreateOrderResponse(
        String orderId,
        String status
) {}
