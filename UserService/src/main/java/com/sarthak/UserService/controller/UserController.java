package com.sarthak.UserService.controller;

import com.sarthak.UserService.request.UserRequest;
import com.sarthak.UserService.response.UserResponse;
import com.sarthak.UserService.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getByUserId(@PathVariable Long userId) {
        UserResponse userResponse = userService.getUserNameById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping()
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

}
