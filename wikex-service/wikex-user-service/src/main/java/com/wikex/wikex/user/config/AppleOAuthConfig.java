package com.wikex.wikex.user.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AppleOAuthConfig {
    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.apple.team-id}")
    private String teamId;

    @Value("${spring.security.oauth2.client.registration.apple.key-id}")
    private String keyId;

    @Value("${spring.security.oauth2.client.registration.apple.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.apple.frontend-url}")
    private String frontendUrl;

    @Value("${spring.security.oauth2.client.registration.apple.private-key-path}")
    private String privateKeyPath;
}
