package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DataMongoTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Autowired
    private UserRepository repo;


    @Mock
    private Exchange mockExchange;

    @Mock
    private ExchangeSupplier supplier;

    private User mockUser = Mocks.user();
    private UserService userService;

    private RefreshToken mockToken = Mocks.refreshToken(10000);

    private Strategy mockStrategy = Mocks.strategy();
    private Map<String, Strategy> strategies = new LinkedHashMap<>();

    private CbotStatus mockStatus = Mocks.cbotStatus();

    @BeforeEach
    public void setup() {
        repo.deleteAll();
        mockUser.setCards(Mocks.cards());
        mockUser.addExchange(ExchangeType.KRAKEN);

        strategies.put(mockStrategy.getName(), mockStrategy);

        mockUser.setStrategies(strategies);

        repo.save(mockUser);
        userService = new UserService(repo, supplier);
    }

    @Test
    void shouldAddCardToUser() {
        userService.addCard(mockUser, Mocks.card());
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getCards().get(Mocks.card().getCardName()),
                samePropertyValuesAs(Mocks.card(), "balances"));
    }

    @Test
    void shouldThrowUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserWithUsername("mockUser4"));
    }

    @Test
    void shouldReplaceUsers() {
        mockUser.setRefreshToken(mockToken);

        userService.replace(mockUser);

        assertThat(repo.getUserByUsername(mockUser.getUsername()).getRefreshToken(), samePropertyValuesAs(mockToken));
    }

    @Test
    void shouldAddStrategyToUser() {
        userService.addStrategy(mockUser, mockStrategy);
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getStrategies().get(mockStrategy.getName()),
                samePropertyValuesAs(mockStrategy));
    }

    @Test
    void shouldChangeUserStatus() {
        userService.updateStatus(mockUser, mockStatus);
        assertThat(repo.getUserByUsername(mockUser.getUsername()).getCbotStatus(), samePropertyValuesAs(mockStatus));
    }

    @Test
    void shouldReturnTradeFeedsGroupedByUserId() throws Exception {
        Trade mockTrade = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ZERO);
        given(supplier.getExchange(any())).willReturn(mockExchange);
        given(mockExchange.getTradeFeed(any())).willReturn(Flux.just(mockTrade));
        given(mockExchange.addUserTradeFeeds(any())).willReturn(mockExchange);

        StepVerifier.create(userService.getTradeFeeds(mockUser))
                .expectSubscription()
                .consumeNextWith(tradeFeed -> assertThat(tradeFeed.key(), is(mockUser.getId())))
                .thenCancel()
                .verify();
    }

}
