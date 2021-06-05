package com.sadoon.cbotback.user.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SignUpRequest {

    private final String username;

    private final String password;

    private final GrantedAuthority authority;

    public SignUpRequest(String username, String password, String authority) {
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
