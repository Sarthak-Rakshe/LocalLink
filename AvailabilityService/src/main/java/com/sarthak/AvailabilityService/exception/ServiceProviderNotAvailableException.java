package com.sarthak.AvailabilityService.exception;

public class ServiceProviderNotAvailableException extends RuntimeException {
    public ServiceProviderNotAvailableException(String message) {
        super(message);
    }
}
