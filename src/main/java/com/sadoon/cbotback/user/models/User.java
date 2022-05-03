package com.sadoon.cbotback.user.models;

import com.sadoon.cbotback.exchange.model.Exchange;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.trade.Trade;
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

    private RefreshToken refreshToken;

    private List<GrantedAuthority> authorities = new ArrayList<>();

    private Map<String, Exchange> cards = new LinkedHashMap<>();

    private Map<String, Strategy> strategies = new LinkedHashMap<>();

    private Map<String, SecurityCredential> encryptedCredentials = new LinkedHashMap<>();

    private List<ExchangeName> exchanges = new ArrayList<>();

    private CbotStatus cbotStatus = new CbotStatus(false, List.of());

    private Map<UUID, Trade> trades = new LinkedHashMap<>();

    public User(String username, String password, GrantedAuthority authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
        authorities.add(authority);
    }

    public String getId() {
        return id;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
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

    public Map<String, Exchange> getCards() {
        return cards;
    }

    public void setCards(Map<String, Exchange> cards) {
        this.cards = cards;
    }

    public Map<String, Strategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(Map<String, Strategy> strategies) {
        this.strategies = strategies;
    }

    public CbotStatus getCbotStatus() {
        return cbotStatus;
    }

    public void setCbotStatus(CbotStatus cbotStatus) {
        this.cbotStatus = cbotStatus;
    }

    public void addEncryptedCredential(String type, SecurityCredential credential) {
        encryptedCredentials.put(type, credential);
    }

    public SecurityCredential getEncryptedCredential(String type) {
        return encryptedCredentials.get(type);
    }

    public List<ExchangeName> getExchanges() {
        return exchanges;
    }

    public void addExchange(ExchangeName exchange) {
        if (!exchanges.contains(exchange)) {
            exchanges.add(exchange);
        }
    }

    public Map<UUID, Trade> getTrades() {
        return trades;
    }

    public void setTrades(Map<UUID, Trade> trades) {
        this.trades = trades;
    }

    public void addTrade(Trade trade) {
        trades.put(trade.getId(), trade);
    }
}
