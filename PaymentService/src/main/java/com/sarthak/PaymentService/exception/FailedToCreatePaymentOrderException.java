package com.sarthak.PaymentService.exception;

public class FailedToCreatePaymentOrderException extends RuntimeException {
    public FailedToCreatePaymentOrderException(String message) {
        super(message);
    }
}
