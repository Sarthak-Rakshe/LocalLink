package com.sarthak.ServiceListingService.exception;

public class DuplicateServiceException extends RuntimeException {
    public DuplicateServiceException(String message) {
        super(message);
    }
}
