package com.sarthak.PaymentService.exception;

public class PayPalWebhookException extends RuntimeException {
    public PayPalWebhookException(String message) {
        super(message);
    }
}
