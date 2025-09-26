package com.sarthak.BookingService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        ExceptionResponse body = new ExceptionResponse("Missing Request Parameter", name + " parameter is missing", "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ExceptionResponse body = new ExceptionResponse("Malformed JSON Request", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = String.format("Parameter '%s' should be of type '%s' but got value '%s'", name, type, value);
        ExceptionResponse body = new ExceptionResponse("Invalid Parameter Type", message, "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());
        ExceptionResponse body = new ExceptionResponse("Validation Error", message, "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponse body = new ExceptionResponse("Internal server exception", ex.getMessage(), "500");
        return ResponseEntity.status(status).body(body);
    }
}
