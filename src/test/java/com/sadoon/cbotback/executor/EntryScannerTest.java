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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class EntryScannerTest {
    @Mock
    private UserService userService;
    @Captor
    private ArgumentCaptor<Trade> tradeCaptor;
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
    void shouldCreateTradeLabel() {
        given(userService.doesUserExist(any())).willReturn(Boolean.TRUE);
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
    void shouldSpecifyIfLongTradesAreWithinRangeOfTarget() {
        Trade lesserValue = Mocks.trade(TradeStatus.ENTRY_SEARCHING, BigDecimal.ZERO, BigDecimal.ONE);
        Trade targetValue = Mocks.trade(TradeStatus.ENTRY_SEARCHING, BigDecimal.ONE, BigDecimal.ONE);
        Trade greaterValue = Mocks.trade(TradeStatus.ENTRY_SEARCHING, BigDecimal.TEN, BigDecimal.ONE);

        assertThat(scanner.isTradeWithinRangeOfTarget(lesserValue), is(true));
        assertThat(scanner.isTradeWithinRangeOfTarget(targetValue), is(true));
        assertThat(scanner.isTradeWithinRangeOfTarget(greaterValue), is(false));
    }

    @Test
    void shouldSpecifyIfShortTradesAreWithinRangeOfTarget() {
        Trade lesserValue =
                Mocks.trade(TradeStatus.ENTRY_SEARCHING, BigDecimal.ZERO, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade targetValue =
                Mocks.trade(TradeStatus.ENTRY_SEARCHING, BigDecimal.ONE, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade greaterValue =
                Mocks.trade(TradeStatus.ENTRY_SEARCHING, BigDecimal.TEN, BigDecimal.ONE).setType(StrategyType.SHORT);

        assertThat(scanner.isTradeWithinRangeOfTarget(lesserValue), is(false));
        assertThat(scanner.isTradeWithinRangeOfTarget(targetValue), is(true));
        assertThat(scanner.isTradeWithinRangeOfTarget(greaterValue), is(true));
    }
}
