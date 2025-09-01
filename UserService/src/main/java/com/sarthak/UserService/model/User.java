package com.sarthak.UserService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotNull
    @Column(unique = true)
    private String userName;

    @NotNull
    @Column(unique = true)
    private String userContact;

    @NotNull
    @Column(unique = true)
    private String userEmail;

    private String userPassword;

    @Enumerated(EnumType.STRING)
    private Types userType;

    public enum Types {
        SELLER,
        CUSTOMER
    }



}
