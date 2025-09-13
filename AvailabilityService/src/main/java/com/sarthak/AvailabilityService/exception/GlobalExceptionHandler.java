package com.sarthak.AvailabilityService.exception;

import com.sarthak.AvailabilityService.dto.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceProviderNotAvailableException.class)
    public ResponseEntity<ExceptionResponse> handleServiceNotFoundException(ServiceProviderNotAvailableException ex) {
        ExceptionResponse body = new ExceptionResponse("Service Provider Not Available", ex.getMessage(), "404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidAvailableTimeSlotParametersException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidParametersException(InvalidAvailableTimeSlotParametersException ex) {
        ExceptionResponse body = new ExceptionResponse("Invalid Parameters", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
