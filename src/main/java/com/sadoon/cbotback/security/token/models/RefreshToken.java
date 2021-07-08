package com.sadoon.cbotback.security.token.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Document
public class RefreshToken {

    private String token = UUID.randomUUID().toString();
    private final String id;

    private final Instant expiryDate;

    public RefreshToken(String id, Instant expiryDate) {
        this.id = id;
        this.expiryDate = expiryDate;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiryDate() {
        return expiryDate.truncatedTo(ChronoUnit.MILLIS);
    }
}
