package com.sadoon.cbotback.refresh.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Document
public class RefreshToken {

    private String token;
    private final String id;

    private final Instant expiryDate;

    public RefreshToken(String id, String token, Instant expiryDate) {
        this.id = id;
        this.token = token;
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
