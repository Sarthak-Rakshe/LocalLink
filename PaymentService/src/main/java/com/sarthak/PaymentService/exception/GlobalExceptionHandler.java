package com.sarthak.PaymentService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("Transaction Not Found", ex.getMessage(), "404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(TransactionReferenceNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionReferenceNotValidException(TransactionReferenceNotValidException ex) {
        ExceptionResponse body = new ExceptionResponse("Transaction Reference Not Valid", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(FailedToCreatePaymentOrderException.class)
    public ResponseEntity<ExceptionResponse> handleFailedToCreatePaymentOrderException(FailedToCreatePaymentOrderException ex) {
        ExceptionResponse body = new ExceptionResponse("Failed To Create Payment Order", ex.getMessage(), "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(UnknownOrderStatusByPayPalException.class)
    public ResponseEntity<ExceptionResponse> handleCreditCardDeclinedException(UnknownOrderStatusByPayPalException ex) {
        ExceptionResponse body = new ExceptionResponse("Unknown status returned by paypal", ex.getMessage(), "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ExceptionResponse> handlePaymentProcessingException(PaymentProcessingException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if(ex.getMessage().toLowerCase().contains("timeout")) {
            status = HttpStatus.GATEWAY_TIMEOUT;
        }
        ExceptionResponse body = new ExceptionResponse("Payment Processing Error", ex.getMessage(), String.valueOf(status.value()));
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        ExceptionResponse body = new ExceptionResponse("Internal Server Error", ex.getMessage(), "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

}
