package com.sarthak.UserService.controller;

import com.sarthak.UserService.dto.request.LoginRequest;
import com.sarthak.UserService.dto.request.UserRegistrationRequest;
import com.sarthak.UserService.dto.response.JwtResponseDto;
import com.sarthak.UserService.dto.response.UserResponse;
import com.sarthak.UserService.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequest request){
        JwtResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request){
        UserResponse response = authService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refreshToken(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refresh");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        JwtResponseDto response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(){
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

}
