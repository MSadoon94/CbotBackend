package com.sadoon.cbotback.user.models;

import com.sadoon.cbotback.card.models.Card;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Document
public class User implements UserDetails {

    @Id
    private String id = UUID.randomUUID().toString();

    private final String username;

    private final String password;

    private final GrantedAuthority authority;

    private List<GrantedAuthority> authorities = new ArrayList<>();

    private Map<String, Card> cards = new LinkedHashMap<>();

    public User(String username, String password, GrantedAuthority authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
        authorities.add(authority);
    }

    public String getId() {
        return id;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Map<String, Card> getCards() {
        return cards;
    }

    public void setCards(Map<String, Card> cards) {
        this.cards = cards;
    }
}
