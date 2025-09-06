package com.sarthak.BookingService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleServiceNotFoundException(BookingNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("Service Not Found", ex.getMessage(), "404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(TimeSlotAlreadyBookedException.class)
    public ResponseEntity<ExceptionResponse> handleTimeSlotAlreadyBookedException(TimeSlotAlreadyBookedException ex) {
        ExceptionResponse body = new ExceptionResponse("Time Slot Already Booked", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ProviderNotAvailableForGivenTimeSlotException.class)
    public ResponseEntity<ExceptionResponse> handleProviderNotAvailableForGivenTimeSlotException(ProviderNotAvailableForGivenTimeSlotException ex) {
        ExceptionResponse body = new ExceptionResponse("Provider Not Available For Given Time Slot", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UnknownAvailabilityStatusException.class)
    public ResponseEntity<ExceptionResponse> handleUnknownAvailabilityStatusException(UnknownAvailabilityStatusException ex) {
        ExceptionResponse body = new ExceptionResponse("Unknown Availability Status", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
