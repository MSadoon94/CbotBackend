package com.sadoon.cbotback.security.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class RegistrationRequest {

    private final String username;

    private final String password;

    private final GrantedAuthority authority;

    public RegistrationRequest(String username, String password, String authority) {
        this.username = username;
        this.password = password;
        this.authority = new SimpleGrantedAuthority(authority);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public GrantedAuthority getAuthority() {
        return authority;
    }
}
