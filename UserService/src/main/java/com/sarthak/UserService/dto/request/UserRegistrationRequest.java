package com.sarthak.UserService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationRequest {

    @NotNull(message = "Username is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Username must contain only letters, numbers, or underscores without spaces"
    )
    private String userName;

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String userEmail;

    @NotNull(message = "Contact is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Contact must be a valid 10-digit number"
    )
    private String userContact;

    @NotNull(message = "UserType is required")
    private String userType;

    @NotNull(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters long and " +
                    "include at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String userPassword;


    @NotNull(message = "Address is required")
    private String userAddress;

    private boolean isActive;

}
