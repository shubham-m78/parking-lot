package com.example.parking.config;

// AudienceValidator: check aud contains clientId

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String clientId;

    public AudienceValidator(String clientId) {
        this.clientId = clientId;
    }

    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (token.getAudience()
                .contains(clientId)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "The required audience is missing", null));
    }
}