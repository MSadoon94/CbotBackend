package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class EntryScannerTest {
    @Mock
    private UserService userService;
    private User mockUser;
    private Authentication auth;

    private EntryScanner scanner;

    @BeforeEach
    public void setUp() {
        mockUser = Mocks.user();
        auth = Mocks.auth(mockUser);
        scanner = new EntryScanner();
    }

    @Test
    void shouldCreateTradeId() {
        Trade targetValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ONE);
        Flux<Trade> tradeFeedIn = Flux.just(targetValue);

        String targetId = String.join("",
                targetValue.getExchange().name(),
                targetValue.getCurrentPrice().toString(),
                targetValue.getPair(),
                targetValue.getTargetPrice().toString(),
                targetValue.getType().name()
        );

        StepVerifier.create(scanner.findEntry(userService, mockUser).apply(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> assertThat(trade.getLabel(), is(targetId)))
                .thenCancel()
                .verify();
    }

    @Test
    void shouldReturnTradeFeedWithLongTradesThatHaveStatusSet() {
        Trade lesserValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ZERO, BigDecimal.ONE);
        Trade targetValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ONE);
        Trade greaterValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.TEN, BigDecimal.ONE);
        Flux<Trade> tradeFeedIn = Flux.just(lesserValue, targetValue, greaterValue);

        StepVerifier.create(scanner.findEntry(userService, mockUser).apply(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> tradeAssert(trade, lesserValue.getCurrentPrice(), TradeStatus.ENTRY_FOUND))
                .consumeNextWith(trade -> tradeAssert(trade, targetValue.getCurrentPrice(), TradeStatus.ENTRY_FOUND))
                .consumeNextWith(trade -> tradeAssert(trade, greaterValue.getCurrentPrice(), TradeStatus.ENTRY_SEARCHING))
                .thenCancel()
                .verify();
    }


    @Test
    void shouldReturnTradeFeedWithShortTradesThatAreInTargetRange() {
        Trade lesserValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ZERO, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade targetValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade greaterValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.TEN, BigDecimal.ONE).setType(StrategyType.SHORT);
        Flux<Trade> tradeFeedIn = Flux.just(lesserValue, targetValue, greaterValue);

        StepVerifier.create(scanner.findEntry(userService, mockUser).apply(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> tradeAssert(trade, lesserValue.getCurrentPrice(), TradeStatus.ENTRY_SEARCHING))
                .consumeNextWith(trade -> tradeAssert(trade, targetValue.getCurrentPrice(), TradeStatus.ENTRY_FOUND))
                .consumeNextWith(trade -> tradeAssert(trade, greaterValue.getCurrentPrice(), TradeStatus.ENTRY_FOUND))
                .thenCancel()
                .verify();
    }

    private void tradeAssert(Trade trade, BigDecimal expectedPrice, TradeStatus expectedStatus) {
        assertThat(trade.getCurrentPrice(), is(expectedPrice));
        assertThat(trade.getStatus(), is(expectedStatus));
    }

}
