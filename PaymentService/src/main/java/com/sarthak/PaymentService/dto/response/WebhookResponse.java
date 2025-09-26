package com.sarthak.PaymentService.dto.response;

import com.sarthak.PaymentService.enums.PaymentStatus;
import lombok.Builder;

@Builder
public record WebhookResponse(
        String orderId,
        PaymentStatus paymentStatus,
        String reason
) {}
