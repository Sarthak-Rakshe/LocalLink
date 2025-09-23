package com.sarthak.UserService.service;

import com.sarthak.UserService.dto.request.LoginRequest;
import com.sarthak.UserService.dto.request.UserRegistrationRequest;
import com.sarthak.UserService.dto.response.JwtResponseDto;
import com.sarthak.UserService.dto.response.UserResponse;
import com.sarthak.UserService.exception.InvalidCredentialsException;
import com.sarthak.UserService.model.UserPrincipal;
import com.sarthak.UserService.repository.UserRepository;
import com.sarthak.UserService.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

        private final UserService userService;
        private final AuthenticationManager authManager;
        private final JwtUtil jwtUtil;

        public UserResponse registerUser(UserRegistrationRequest request){
            return userService.registerUser(request);
        }

        public JwtResponseDto login(LoginRequest request){

            try {
                Authentication authentication = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username().toLowerCase(),
                                request.password()
                        )
                );

                UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

                String accessToken = jwtUtil.generateToken(user);
                String refreshToken = jwtUtil.generateRefreshToken(user);

                UserResponse response = userService.getUserByUsername(user.getUsername());

                log.info("User {} logged in successfully", request.username());

                return JwtResponseDto.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .userType(user.getUserType().name())
                        .tokenType("Bearer")
                        .build();

            }catch (BadCredentialsException e){
                log.error("Failed login attempt for: {}", request.username());
                throw new BadCredentialsException("Invalid username or password");
            }

        }


        public JwtResponseDto refreshToken(String refreshToken){
            try{
                if (!jwtUtil.isTokenValid(refreshToken)){
                    throw new InvalidCredentialsException("Invalid refresh token");
                }

                String username = jwtUtil.extractUsername(refreshToken);
                UserPrincipal user = userService.loadUserByUsername(username);

                String newAccessToken = jwtUtil.generateToken(user);
                String newRefreshToken = jwtUtil.generateRefreshToken(user);

                UserResponse response = userService.getUserByUsername(user.getUsername());

                return JwtResponseDto.builder()
                        .token(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .userType(user.getUserType().name())
                        .tokenType("Bearer")
                        .build();

            }catch (Exception e){
                log.error("Invalid refresh token attempt {}", e.getMessage());
                throw new InvalidCredentialsException("Invalid refresh token");
            }
        }


}
