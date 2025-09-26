package com.sarthak.PaymentService.config.shared;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenValidator {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Long extractUserId(String jwt) {
        return extractClaim(jwt, claims -> claims.get("userId", Long.class));
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token){
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractUserType(String token){
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public  <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("JWT token expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new RuntimeException("Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            throw new RuntimeException("Malformed JWT token", e);
        } catch (SecurityException | IllegalArgumentException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT signature", e);
        }
    }

    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token){
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        }catch (Exception e){
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public List<String> extractAuthorities(String token){
        String role = extractRole(token);
        return role != null ? List.of(role) : List.of();
    }

}
