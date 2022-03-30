package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class EntryScannerTest {

    private EntryScanner scanner;
    @MockBean
    private Logger logger;

    @BeforeEach
    public void setUp() {

        scanner = new EntryScanner();
    }

    @Test
    void shouldReturnTradeFeedWithLongTradesThatAreInTargetRange() {
        Trade lesserValue = Mocks.trade(true, BigDecimal.ZERO, BigDecimal.ONE);
        Trade targetValue = Mocks.trade(true, BigDecimal.ONE, BigDecimal.ONE);
        Trade greaterValue = Mocks.trade(true, BigDecimal.TEN, BigDecimal.ONE);
        Flux<Trade> tradeFeedIn = Flux.just(lesserValue, targetValue, greaterValue);

        StepVerifier.create(scanner.tradeFeed(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> assertThat(trade.getCurrentPrice(), is(targetValue.getCurrentPrice())))
                .consumeNextWith(trade -> assertThat(trade.getCurrentPrice(), is(greaterValue.getCurrentPrice())))
                .thenCancel()
                .verify();
    }

    @Test
    void shouldReturnTradeFeedWithShortTradesThatAreInTargetRange() {
        Trade lesserValue = Mocks.trade(true, BigDecimal.ZERO, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade targetValue = Mocks.trade(true, BigDecimal.ONE, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade greaterValue = Mocks.trade(true, BigDecimal.TEN, BigDecimal.ONE).setType(StrategyType.SHORT);
        Flux<Trade> tradeFeedIn = Flux.just(lesserValue, targetValue, greaterValue);

        StepVerifier.create(scanner.tradeFeed(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> assertThat(trade.getCurrentPrice(), is(lesserValue.getCurrentPrice())))
                .consumeNextWith(trade -> assertThat(trade.getCurrentPrice(), is(targetValue.getCurrentPrice())))
                .thenCancel()
                .verify();
    }

}
