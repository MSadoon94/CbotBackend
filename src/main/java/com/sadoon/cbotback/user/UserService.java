package com.sadoon.cbotback.user;

import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import com.sadoon.cbotback.exp.TradeListener;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.user.models.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class UserService {

    private UserRepository repo;
    private ExchangeSupplier exchangeSupplier;
    private Map<String, Map<String, SecurityCredential>> cachedCredentials = new LinkedHashMap<>();

    public UserService(UserRepository repo, ExchangeSupplier exchangeSupplier) {
        this.repo = repo;
        this.exchangeSupplier = exchangeSupplier;
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

    public Flux<GroupedFlux<String, Flux<Trade>>> getTradeFeeds(User user) {
        return Flux.fromIterable(user.getExchanges())
                .map(exchangeSupplier::getExchange)
                .flatMap(exchange -> Mono.fromCallable(() ->
                                exchange.getTradeFeed(
                                        user,
                                        cachedCredentials.get(user.getId())
                                                .get(exchange.getExchangeName().name())))
                        .flux()
                        .groupBy(feed -> user.getId())
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(exchange::addUserTradeFeeds));
    }

    public Flux<Trade> getTradeFeed(User user) {
        return Flux.fromIterable(user.getExchanges())
                .map(exchangeSupplier::getExchange)
                .flatMap(exchange -> Mono.fromCallable(() ->
                        exchange.getTradeFeed(
                                user,
                                cachedCredentials.get(user.getId())
                                        .get(exchange.getExchangeName().name()))))
                .flatMap(Function.identity());
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

    public void addCard(User user, Card card) {
        Map<String, Card> cards = user.getCards();
        cards.put(card.getCardName(), card);
        user.setCards(cards);
        replace(user);
    }

    public void addStrategy(User user, Strategy strategy) {
        Map<String, Strategy> strategies = user.getStrategies();
        strategies.put(strategy.getName(), strategy);
        user.setStrategies(strategies);
        user.addExchange(ExchangeName.valueOf(strategy.getExchange().toUpperCase()));
        replace(user);
    }

    public void addTrade(User user, Trade trade) {
        Map<UUID, Trade> trades = user.getTrades();
        trades.put(trade.getId(), trade);
        user.setTrades(trades);
        replace(user);
    }

    public void addEncryptedCredential(User user, SecurityCredential credential) {
        user.addEncryptedCredential(credential.type(), credential);
        replace(user);
        if (Arrays.stream(ExchangeName.values())
                .anyMatch(name -> name == ExchangeName.valueOf(credential.type()))) {
            new TradeListener(
                    this,
                    exchangeSupplier.getExchange(ExchangeName.valueOf(credential.type())),
                    user
            ).start();
        }
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
