package com.example.parking.security;

import com.example.parking.config.SecurityProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class GoogleJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final SecurityProperties securityProperties;

    public GoogleJwtRoleConverter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            return List.of();
        }

        String normalized = email.trim()
                .toLowerCase();

        if (securityProperties.getAdminEmails()
                .stream()
                .map(String::toLowerCase)
                .anyMatch(normalized::equals)) {
            // Admins get both ADMIN and USER roles
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }

        if (securityProperties.getUserEmails()
                .stream()
                .map(String::toLowerCase)
                .anyMatch(normalized::equals)) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Default fallback
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}