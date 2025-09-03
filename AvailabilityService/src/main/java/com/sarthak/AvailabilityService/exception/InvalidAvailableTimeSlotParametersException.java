package com.sarthak.AvailabilityService.exception;

public class InvalidAvailableTimeSlotParametersException extends RuntimeException {
    public InvalidAvailableTimeSlotParametersException(String message) {
        super(message);
    }
}
