package com.sarthak.ServiceListingService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleServiceNotFoundException(ServiceNotFoundException ex) {
        ExceptionResponse response = ExceptionResponse.builder()
                .error("Service Not Found")
                .message(ex.getMessage())
                .status(404)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateServiceException.class)
    public ResponseEntity<ExceptionResponse> handleDuplicateServiceException(DuplicateServiceException ex) {
        ExceptionResponse response = ExceptionResponse.builder()
                .error("Duplicate Entity")
                .message(ex.getMessage())
                .status(409)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
