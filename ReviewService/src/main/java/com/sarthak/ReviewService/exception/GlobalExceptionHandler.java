package com.sarthak.ReviewService.exception;

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

    public record ExceptionResponse(String error, String message, int status){}

    @ExceptionHandler(DuplicateReviewForSameServiceException.class)
    public ExceptionResponse handleDuplicateReview(DuplicateReviewForSameServiceException ex){
        return new ExceptionResponse("Duplicate Entity", ex.getMessage(),  HttpStatus.CONFLICT.value());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ExceptionResponse handleEntityNotFound(EntityNotFoundException ex){
        return new ExceptionResponse("Entity Not Found", ex.getMessage(),  HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(BookingNotCompleteException.class)
    public ExceptionResponse bookingNotComplete (BookingNotCompleteException ex) {
        return new ExceptionResponse("Booking is not completed", ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        ExceptionResponse body = new ExceptionResponse("Missing Request Parameter", name + " parameter is missing", 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ExceptionResponse body = new ExceptionResponse("Malformed JSON Request", ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = String.format("Parameter '%s' should be of type '%s' but got value '%s'", name, type, value);
        ExceptionResponse body = new ExceptionResponse("Invalid Parameter Type", message, 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());
        ExceptionResponse body = new ExceptionResponse("Validation Error", message, 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponse body = new ExceptionResponse("Internal server exception", ex.getMessage(), 500);
        return ResponseEntity.status(status).body(body);
    }

}
