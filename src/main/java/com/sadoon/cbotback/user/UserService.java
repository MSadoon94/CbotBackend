package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.user.models.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {
    private UserRepository repo;
    private Map<String, Map<String, SecurityCredential>> cachedCredentials = new LinkedHashMap<>();
    private Sinks.Many<Trade> tradeFeed = Sinks.many().multicast().onBackpressureBuffer();
    private Sinks.Many<Trade> updatedTrades = Sinks.many().multicast().onBackpressureBuffer();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User getUserWithUsername(String username) throws UserNotFoundException {
        return checkForNullUser(repo.getUserByUsername(username), username);
    }

    public User getUserWithId(String id) throws UserNotFoundException {
        return checkForNullUser(repo.getUserById(id), id);
    }

    private User checkForNullUser(User user, String username) throws UserNotFoundException {
        if (user == null) {
            throw new UserNotFoundException(username);
        } else {
            return user;
        }
    }

    public User updateStatus(User user, CbotStatus status) {
        user.setCbotStatus(status);
        return replace(user);
    }

    public SecurityCredential getCredential(User user, String type) {
        return cachedCredentials.get(user.getId()).get(type);
    }

    public void deleteAll() {
        repo.deleteAll();
    }

    public User save(User user) {
        return repo.save(user);
    }

    public User replace(User user) {
        repo.deleteById(user.getId());
        return repo.save(user);
    }

    public void addStrategy(User user, Strategy strategy) {
        Map<String, Strategy> strategies = user.getStrategies();
        strategies.put(strategy.getName(), strategy);
        user.setStrategies(strategies);
        user.addExchange(ExchangeName.valueOf(strategy.getExchange().toUpperCase()));
        replace(user);
    }

    public Flux<Trade> userTradeFeed(User user) {
        return tradeFeed
                .asFlux()
                /*.filter(trade -> user.getTrades().containsKey(trade.getId()))*/;

    }

    public Flux<Trade> userTradeUpdateFeed(User user) {
        return updatedTrades
                .asFlux()
                /*.filter(trade -> user.getTrades().containsKey(trade.getId()))*/;
    }

    public void addTrade(User user, Trade trade) {
        user.addTrade(trade);
        replace(user);
        tradeFeed.tryEmitNext(trade);
    }

    public void updateTrade(User user, Trade trade) {
        user.addTrade(trade);
        replace(user);
        updatedTrades.tryEmitNext(trade);
    }

    public void addEncryptedCredential(User user, SecurityCredential credential) {
        user.addEncryptedCredential(credential.type(), credential);
        replace(user);
    }

    public void cacheCredential(User user, SecurityCredential credential) {
        Map<String, SecurityCredential> credentials = new LinkedHashMap<>();
        if (cachedCredentials.containsKey(user.getId())) {
            credentials = cachedCredentials.get(user.getId());
        }
        credentials.put(credential.type(), credential);
        cachedCredentials.put(user.getId(), credentials);
    }
}
