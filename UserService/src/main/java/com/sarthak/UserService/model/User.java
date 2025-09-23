package com.sarthak.UserService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "user_email"),
        @Index(name = "idx_user_contact", columnList = "user_contact")
},
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uk_user_email", columnNames = {"user_email"}),
        @UniqueConstraint(name = "uk_user_contact", columnNames = {"user_contact"}),
        @UniqueConstraint(name = "uk_email_contact_type", columnNames = {"user_email", "user_contact", "user_type"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "user_contact", unique = true, nullable = false)
    private String userContact;

    @Email(message = "Email should be valid")
    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @Column(name = "user_password", nullable = false)
    private String userPassword;

    @Column(name = "user_address", nullable = false)
    private String userAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, updatable = false)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "user_role", nullable = false, updatable = false)
    private UserRole userRole = UserRole.USER;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}

