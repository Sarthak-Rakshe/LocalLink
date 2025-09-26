package com.sarthak.ServiceListingService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
