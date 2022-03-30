package com.sadoon.cbotback.user.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.credentials.SecurityCredentials;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
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

    private Map<String, Card> cards = new LinkedHashMap<>();

    private Map<String, Strategy> strategies = new LinkedHashMap<>();

    private Map<String, SecurityCredentials> credentials = new LinkedHashMap<>();

    private List<ExchangeType> exchanges = new ArrayList<>();

    private CbotStatus cbotStatus = new CbotStatus(false, List.of());

    private Map<String, Trade> trades = new LinkedHashMap<>();

    @JsonIgnore
    private Flux<Trade> tradeFeed;

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

    public Map<String, Card> getCards() {
        return cards;
    }

    public void setCards(Map<String, Card> cards) {
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

    public void setCredential(String type, SecurityCredentials credential) {
        credentials.put(type, credential);
    }

    public SecurityCredentials getCredential(String type) {
        return credentials.get(type);
    }

    public List<ExchangeType> getExchanges() {
        return exchanges;
    }

    public void addExchange(ExchangeType exchange) {
        exchanges.add(exchange);
    }

    public Map<String, Trade> getTrades() {
        return trades;
    }

    public void setTrades(Map<String, Trade> trades) {
        this.trades = trades;
    }

    @JsonIgnore
    public Flux<Trade> getTradeFeed(){
        if(tradeFeed == null){
            createTrades();
        }
        return tradeFeed;
    }

    @JsonIgnore
    public void createTrades() {
        tradeFeed = Flux.fromStream(
                        strategies
                                .values()
                                .stream())
                .flatMap(strategy ->
                        Mono.fromCallable(() ->
                                        new Trade()
                                                .setActive(cbotStatus.activeStrategies().contains(strategy.getName()))
                                                .setPair(strategy.getPair())
                                                .setType(strategy.asStrategyType())
                                                .setEntryPercentage(new BigDecimal(strategy.getEntry()))
                                )
                                .onErrorResume(Mono::error)
                                .subscribeOn(Schedulers.boundedElastic()))
            .takeWhile(trade -> cbotStatus.isActive());
    }
}
