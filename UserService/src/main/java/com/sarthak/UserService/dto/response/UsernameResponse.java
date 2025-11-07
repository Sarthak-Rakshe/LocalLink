package com.sarthak.UserService.dto.response;

import lombok.Builder;

@Builder
public record UsernameResponse (
        String username,
        String userContact,
        Long userId
){}
