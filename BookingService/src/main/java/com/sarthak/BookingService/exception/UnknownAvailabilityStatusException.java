package com.sarthak.BookingService.exception;

public class UnknownAvailabilityStatusException extends RuntimeException {
    public UnknownAvailabilityStatusException(String message) {
        super(message);
    }
}
