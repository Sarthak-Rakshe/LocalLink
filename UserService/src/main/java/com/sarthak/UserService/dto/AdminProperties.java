package com.sarthak.UserService.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private String username;
    private String password;
    private String email;

}
