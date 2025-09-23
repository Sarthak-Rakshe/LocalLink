package com.sarthak.UserService.dto.response;

import lombok.*;

@Builder
public record ExceptionResponse (
        String msgName,
        String message,
        String statusCode
){}
