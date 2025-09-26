package com.sarthak.UserService.dto.response;

import lombok.Builder;

@Builder
public record JwtResponseDto(
        String token,
        String refreshToken,
        String tokenType,
        UserResponse userResponse
) {}
