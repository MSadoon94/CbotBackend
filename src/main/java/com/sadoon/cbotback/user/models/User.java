package com.sadoon.cbotback.user.models;

import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

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

    private Map<String, SecurityCredential> encryptedCredentials = new LinkedHashMap<>();

    private List<ExchangeName> exchanges = new ArrayList<>();

    private CbotStatus cbotStatus = new CbotStatus(false, List.of());

    private Map<UUID, Trade> trades = new LinkedHashMap<>();

    @Transient
    private Sinks.Many<Trade> tradeFeed2 = Sinks.many().multicast().onBackpressureBuffer();

    @Transient
    private Sinks.Many<ExchangeName> exchangeFeed = Sinks.many().multicast().onBackpressureBuffer();

    @Transient
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
            exchangeFeed.tryEmitNext(exchange);
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
        tradeFeed2.tryEmitNext(trade);
    }

    public Flux<Trade> getTradeFeed2() {
        return tradeFeed2
                .asFlux()
                .share();
    }

    public Flux<ExchangeName> getExchangeFeed() {
        return exchangeFeed
                .asFlux()
                .share();
    }

    public Flux<Trade> getTradeFeed() {
        if (tradeFeed == null) {
            createTrades();
        }
        return tradeFeed.share();
    }

    public void createTrades() {
        tradeFeed = Flux.fromStream(
                        strategies
                                .values()
                                .stream())
                .flatMap(strategy ->
                        Mono.fromCallable(() ->
                                new Trade()
                                        .setStatus(getTradeStatusFromActiveStrategies(strategy))
                                        .setPair(strategy.getPair())
                                        .setType(strategy.asStrategyType())
                                        .setEntryPercentage(new BigDecimal(strategy.getEntry()))
                        ));
    }

    private TradeStatus getTradeStatusFromActiveStrategies(Strategy strategy) {
        if (strategy.isActive()) {
            return TradeStatus.SELECTED;
        } else {
            return TradeStatus.NOT_SELECTED;
        }
    }
}
