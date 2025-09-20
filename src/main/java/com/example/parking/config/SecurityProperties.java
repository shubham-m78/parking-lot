package com.example.parking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityProperties {

    private List<String> adminEmails = new ArrayList<>();
    private List<String> userEmails = new ArrayList<>();
}
