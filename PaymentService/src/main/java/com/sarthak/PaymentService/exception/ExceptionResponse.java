package com.sarthak.PaymentService.exception;

public record ExceptionResponse(
        String msgName,
        String message,
        int statusCode
) {}
