package com.sarthak.ServiceListingService.exception;

import com.sarthak.ServiceListingService.response.ExceptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleServiceNotFoundException(ServiceNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("Service Not Found", ex.getMessage(), "404");
        return ResponseEntity.status(404).body(body);
    }
}
