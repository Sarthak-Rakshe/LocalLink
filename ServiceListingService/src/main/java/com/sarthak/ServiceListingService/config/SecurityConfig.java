package com.sarthak.ServiceListingService.config;

import com.sarthak.ServiceListingService.config.shared.JwtAuthenticationEntryPoint;
import com.sarthak.ServiceListingService.config.shared.JwtValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtValidationFilter jwtValidationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public UserDetailsService userDetailsService(){
        return username -> null;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws  Exception{

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests( auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/services/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/services/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
