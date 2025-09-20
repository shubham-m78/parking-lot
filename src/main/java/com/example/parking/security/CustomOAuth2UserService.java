package com.example.parking.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Default service to fetch user info from Google
        OAuth2User oauth2User = new org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService()
                .loadUser(userRequest);

        String email = oauth2User.getAttribute("email");

        // Assign roles
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (email != null && email.endsWith("@admin.com")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "sub");
    }
}