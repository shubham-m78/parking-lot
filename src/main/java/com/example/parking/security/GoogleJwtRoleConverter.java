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

    private final SecurityProperties props;

    public GoogleJwtRoleConverter(SecurityProperties props) {
        this.props = props;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            return List.of();
        }

        String normalized = email.trim().toLowerCase();

        if (props.getAdminEmails().stream().map(String::toLowerCase).anyMatch(normalized::equals)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (props.getUserEmails().stream().map(String::toLowerCase).anyMatch(normalized::equals)) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // default fallback
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}