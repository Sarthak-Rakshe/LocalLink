package com.sarthak.UserService.service;

import com.sarthak.UserService.exception.UserNotFoundException;
import com.sarthak.UserService.mapper.UserMapper;
import com.sarthak.UserService.model.User;
import com.sarthak.UserService.repository.UserRepository;
import com.sarthak.UserService.dto.request.UserRequest;
import com.sarthak.UserService.dto.response.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public  UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getUserNameById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found"));
        return userMapper.UserToUserResponse(user);
    }

    public UserResponse createUser(UserRequest userRequest) {
        User user = userMapper.UserRequestToUser(userRequest);
        User savedUser = userRepository.save(user);
        return userMapper.UserToUserResponse(savedUser);
    }

}
