package com.sadoon.cbotback;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String[] endpointExclusions;
    private String corsExclusion;

    private String jwtSecret;
    private Long jwtExpirationMs = 900000L;
    private Long refreshTokenDurationMs = 1800000L;

    private String krakenApiKey;
    private String krakenSecretKey;

    private String keystoreName;
    private String keystorePassword;

    public String getCorsExclusion() {
        return corsExclusion;
    }

    public void setCorsExclusion(String corsExclusion) {
        this.corsExclusion = corsExclusion;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public void setJwtExpirationMs(Long jwtExpirationMs) {
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String getKrakenApiKey() {
        return krakenApiKey;
    }

    public void setKrakenApiKey(String krakenApiKey) {
        this.krakenApiKey = krakenApiKey;
    }

    public String getKrakenSecretKey() {
        return krakenSecretKey;
    }

    public void setKrakenSecretKey(String krakenSecretKey) {
        this.krakenSecretKey = krakenSecretKey;
    }

    public Long getRefreshTokenDurationMs() {
        return refreshTokenDurationMs;
    }

    public void setRefreshTokenDurationMs(Long refreshTokenDurationMs) {
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    public String getKeystoreName() {
        return keystoreName;
    }

    public void setKeystoreName(String keystoreName) {
        this.keystoreName = keystoreName;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String[] getEndpointExclusions() {
        return endpointExclusions;
    }

    public void setEndpointExclusions(String[] endpointExclusions) {
        this.endpointExclusions = endpointExclusions;
    }
}
