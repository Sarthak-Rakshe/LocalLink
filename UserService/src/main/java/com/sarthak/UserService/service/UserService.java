package com.sarthak.UserService.service;

import com.sarthak.UserService.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public  UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getUserNameById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found")).getUserName();
    }


}
