package com.sarthak.AvailabilityService.exception;

public class ConflictingRulesException extends RuntimeException {
    public ConflictingRulesException(String message) {
        super(message);
    }
}
