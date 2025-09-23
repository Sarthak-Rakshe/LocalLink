package com.sarthak.UserService.mapper;

import com.sarthak.UserService.dto.response.UserResponse;
import com.sarthak.UserService.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .userEmail(user.getUserEmail())
                .userContact(user.getUserContact())
                .userType(user.getUserType().name())
                .build();
    }
}
