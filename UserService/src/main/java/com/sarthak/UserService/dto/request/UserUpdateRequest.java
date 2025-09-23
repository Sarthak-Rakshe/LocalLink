package com.sarthak.UserService.dto.request;

public record UserUpdateRequest (
        String username,
        String userEmail,
        String userContact,
        String userAddress
){}
