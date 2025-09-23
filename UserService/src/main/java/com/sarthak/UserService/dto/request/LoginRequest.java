package com.sarthak.UserService.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoginRequest (
        @NotNull String username,
        @NotNull String password
){}
