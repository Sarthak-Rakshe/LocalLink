package com.sarthak.BookingService.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleServiceNotFoundException(BookingNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("Service Not Found", ex.getMessage(), "404");
        return ResponseEntity.status(404).body(body);
    }
}
