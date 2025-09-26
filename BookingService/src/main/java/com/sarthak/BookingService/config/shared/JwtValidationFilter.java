package com.sarthak.BookingService.config.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if(!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            if(jwtTokenValidator.isTokenValid(jwt) && SecurityContextHolder.getContext().getAuthentication() == null){

                Long userId = jwtTokenValidator.extractUserId(jwt);
                String username = jwtTokenValidator.extractUsername(jwt);
                String role = jwtTokenValidator.extractRole(jwt);
                String type = jwtTokenValidator.extractUserType(jwt);

                UserPrincipal userPrincipal = UserPrincipal.builder()
                        .userId(userId)
                        .username(username)
                        .userRole(role)
                        .userType(type)
                        .build();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }catch (Exception e){
            log.warn("Error validating JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);

    }
}
