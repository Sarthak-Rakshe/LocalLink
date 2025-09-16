package com.sarthak.AvailabilityService.exception;

public class InvalidTimeSlotParametersException extends RuntimeException {
    public InvalidTimeSlotParametersException(String message) {
        super(message);
    }
}
