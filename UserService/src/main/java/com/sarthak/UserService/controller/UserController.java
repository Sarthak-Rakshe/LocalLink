package com.sarthak.UserService.controller;

import com.sarthak.UserService.model.User;
import com.sarthak.UserService.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("name")
    public ResponseEntity<> getUserName(Long userId) {
        return userService.getUserNameById(userId);
    }

}
