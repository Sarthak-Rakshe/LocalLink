package com.sarthak.AvailabilityService.exception;

import com.sarthak.AvailabilityService.dto.response.ExceptionResponse;
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

    @ExceptionHandler(ServiceProviderNotAvailableException.class)
    public ResponseEntity<ExceptionResponse> handleServiceNotFoundException(ServiceProviderNotAvailableException ex) {
        ExceptionResponse body = new ExceptionResponse("Service Provider Not Found", ex.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidTimeSlotParametersException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidParametersException(InvalidTimeSlotParametersException ex) {
        ExceptionResponse body = new ExceptionResponse("Invalid Parameters", ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidDayOfWeekException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidDayOfWeekException(InvalidDayOfWeekException ex) {
        ExceptionResponse body = new ExceptionResponse("Invalid Day of Week", ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ExceptionResponse> handleDuplicateEntityException(DuplicateEntityException ex) {
        ExceptionResponse body = new ExceptionResponse("Duplicate Entity", ex.getMessage(), 409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("Entity Not Found", ex.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ServiceClientResponseMismatchException.class)
    public ResponseEntity<ExceptionResponse> handleResponseValueMismatchForRequestException(ServiceClientResponseMismatchException ex) {
        ExceptionResponse body = new ExceptionResponse("Service client sent incorrect response", ex.getMessage(),
                502);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(ConflictingRulesException.class)
    public ResponseEntity<ExceptionResponse> handleConflictingRules(ConflictingRulesException ex){
        ExceptionResponse body = new ExceptionResponse("Conflicting Rules", ex.getMessage(), 409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
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
        ExceptionResponse body = new ExceptionResponse("Internal Server Error", ex.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
