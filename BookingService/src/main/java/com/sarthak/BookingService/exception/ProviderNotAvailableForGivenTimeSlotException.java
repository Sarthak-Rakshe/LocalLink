package com.sarthak.BookingService.exception;

public class ProviderNotAvailableForGivenTimeSlotException extends RuntimeException {
    public ProviderNotAvailableForGivenTimeSlotException(String message) {
        super(message);
    }
}
