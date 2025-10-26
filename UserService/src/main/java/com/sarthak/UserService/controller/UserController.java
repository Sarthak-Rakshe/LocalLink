package com.sarthak.UserService.controller;

import com.sarthak.UserService.dto.PagedResponse;
import com.sarthak.UserService.dto.request.UserUpdateRequest;
import com.sarthak.UserService.dto.response.ProviderResponse;
import com.sarthak.UserService.dto.response.UserResponse;
import com.sarthak.UserService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getByUserId(@PathVariable Long userId) {
        UserResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String name = authentication.getName();
        UserResponse userResponse = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<UserResponse> getAllUsers(
            @RequestParam(name = "sort-by", defaultValue = "id") String sortBy,
            @RequestParam(name = "sort-dir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, sortDir);

        return new PagedResponse<>(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        UserResponse updatedUser = userService.deactivateUser(id);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        UserResponse updatedUser = userService.activateUser(id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkUserExists(@RequestParam(required = false) String username,
                                                   @RequestParam(required = false) String email,
                                                   @RequestParam(required = false) String contact) {
        if (username != null){
            return ResponseEntity.ok(userService.existsByUsername(username));
        } else if (email != null) {
            return ResponseEntity.ok(userService.existsByEmail(email));
        } else if (contact != null) {
            return ResponseEntity.ok(userService.existsByContact(contact));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/getProviders")
    public PagedResponse<ProviderResponse> getProviders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort-by", defaultValue = "id") String sortBy,
            @RequestParam(name = "sort-dir", defaultValue = "asc") String sortDir
    ){
        Page<ProviderResponse> providers = userService.getProviders(page, size, sortBy, sortDir);
        return new PagedResponse<>(
                providers.getContent(),
                providers.getNumber(),
                providers.getSize(),
                providers.getTotalElements(),
                providers.getTotalPages()
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ProviderResponse> getProviderById(@PathVariable Long providerId) {
        ProviderResponse providerResponse = userService.getProviderById(providerId);
        return ResponseEntity.ok(providerResponse);
    }

}
