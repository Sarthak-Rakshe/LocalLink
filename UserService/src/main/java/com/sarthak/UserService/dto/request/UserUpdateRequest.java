package com.sarthak.UserService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest (
        @NotNull(message = "Username is required")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "Username must contain only letters, numbers, or underscores without spaces"
        )
        String username,

        @NotNull(message = "Email is required")
        @Email(message = "Email should be valid")
        String userEmail,
        @NotNull(message = "Contact is required")
        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Contact must be a valid 10-digit number"
        )
        String userContact,
        @NotNull(message = "Address is required")
        String userAddress
){}
