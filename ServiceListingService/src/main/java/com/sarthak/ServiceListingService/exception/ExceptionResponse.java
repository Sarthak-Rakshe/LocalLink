package com.sarthak.ServiceListingService.exception;

import lombok.*;

@Builder
public record ExceptionResponse(
        String error,
        String message,
        int status
) {}
