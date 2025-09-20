package com.sarthak.AvailabilityService.dto.response;

import lombok.*;

@Builder
public record ExceptionResponse(
        String msgName,
        String message,
        String statusCode
) {}
