package com.sarthak.PaymentService.exception;

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

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("Transaction Not Found", ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(TransactionReferenceNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionReferenceNotValidException(TransactionReferenceNotValidException ex) {
        ExceptionResponse body = new ExceptionResponse("Transaction Reference Not Valid", ex.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(FailedToCreatePaymentOrderException.class)
    public ResponseEntity<ExceptionResponse> handleFailedToCreatePaymentOrderException(FailedToCreatePaymentOrderException ex) {
        ExceptionResponse body = new ExceptionResponse("Failed To Create Payment Order", ex.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(PayPalWebhookException.class)
    public ResponseEntity<ExceptionResponse> handleCreditCardDeclinedException(PayPalWebhookException ex) {
        ExceptionResponse body = new ExceptionResponse("Unknown status returned by paypal", ex.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ExceptionResponse> handlePaymentProcessingException(PaymentProcessingException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if(ex.getMessage().toLowerCase().contains("timeout")) {
            status = HttpStatus.GATEWAY_TIMEOUT;
        }
        ExceptionResponse body = new ExceptionResponse("Payment Processing Error", ex.getMessage(), status.value());
        return ResponseEntity.status(status).body(body);
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

    @ExceptionHandler(BookingClientException.class)
    public ResponseEntity<ExceptionResponse> handleBookingClientException(BookingClientException ex) {
        ExceptionResponse body = new ExceptionResponse("Booking Service Client Error", ex.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
