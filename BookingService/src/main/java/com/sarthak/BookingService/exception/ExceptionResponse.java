package com.sarthak.BookingService.exception;

import lombok.Builder;

@Builder
public record ExceptionResponse (
        String msgName,
        String message,
        String statusCode
){}
