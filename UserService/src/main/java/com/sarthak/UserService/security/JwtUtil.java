package com.sarthak.UserService.security;

import com.sarthak.UserService.model.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}" )
    private String secretKey;

    @Value("${jwt.expiration:3600000}" )
    private Long jwtExpirationInMs;

    @Value("${jwt.refresh.expiration:86400000}" )
    private Long refreshExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token){
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractUserType(String token){
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException | IllegalArgumentException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT signature", e);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserPrincipal userPrincipal){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userPrincipal.getAuthorities().iterator().next().getAuthority());
        claims.put("type", userPrincipal.getUserType());
        return createToken(claims, userPrincipal.getUsername(), jwtExpirationInMs);
    }

    public String generateRefreshToken(UserPrincipal userPrincipal){
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");
        return createToken(claims, userPrincipal.getUsername(), refreshExpirationInMs);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        try{
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }catch (Exception e){
            log.warn("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token){
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        }catch (Exception e){
            return false;
        }
    }


}
