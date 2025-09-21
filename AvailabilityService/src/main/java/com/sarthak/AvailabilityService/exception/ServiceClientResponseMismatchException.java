package com.sarthak.AvailabilityService.exception;

public class ServiceClientResponseMismatchException extends RuntimeException {
    public ServiceClientResponseMismatchException(String message) {
        super(message);
    }
}
