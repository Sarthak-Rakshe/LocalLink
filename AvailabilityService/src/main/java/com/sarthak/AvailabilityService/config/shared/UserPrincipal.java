package com.sarthak.AvailabilityService.config.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long userId;
    private String username;
    private String userRole;
    private String userType;
    private List<String> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(authorities != null && !authorities.isEmpty()){
            return authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        return userRole != null ? List.of(new SimpleGrantedAuthority(userRole)) : List.of();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
