package com.sarthak.PaymentService.dto.response;

import lombok.Builder;

@Builder
public record CaptureOrderResponse(
        String orderId,
        String status
) {}
