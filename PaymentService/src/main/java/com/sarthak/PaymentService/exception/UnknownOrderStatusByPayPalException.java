package com.sarthak.PaymentService.exception;

public class UnknownOrderStatusByPayPalException extends RuntimeException {
    public UnknownOrderStatusByPayPalException(String message) {
        super(message);
    }
}
