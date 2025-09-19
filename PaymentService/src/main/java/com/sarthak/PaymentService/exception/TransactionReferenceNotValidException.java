package com.sarthak.PaymentService.exception;

public class TransactionReferenceNotValidException extends RuntimeException {
    public TransactionReferenceNotValidException(String message) {
        super(message);
    }
}
