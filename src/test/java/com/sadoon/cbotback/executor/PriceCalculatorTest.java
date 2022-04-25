package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@ExtendWith(MockitoExtension.class)
class PriceCalculatorTest {

    private PriceCalculator calculator;

    private Strategy mockStrategy = Mocks.strategy();
    private String entry = String.valueOf(Math.random());
    private String price = String.valueOf(Math.floor(Math.random() * ((1000000 - 1 + 1) + 1)));
    private Trade mockTrade = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ZERO);
    private String[] mockValues = new String[]{price, "1", "1"};

    @BeforeEach
    public void setUp() {
        mockStrategy.setExchange("mockBrokerage");
        calculator = new PriceCalculator();
    }

    @RepeatedTest(10)
    void shouldReturnTradeForLongStrategiesWithTargetPriceSet() {
        mockTrade.setCurrentPrice(new BigDecimal(price))
                .setEntryPercentage(new BigDecimal(mockStrategy.getEntry()))
                .setFees(Mocks.fees());

        Flux<Trade> tradeFeedIn = Flux.just(mockTrade);

        BigDecimal solution =
                new BigDecimal(Mocks.krakenTickerMessage(mockValues).getBid()).subtract(
                                (new BigDecimal(mockStrategy.getEntry()).movePointLeft(2))
                                        .multiply(new BigDecimal(Mocks.krakenTickerMessage(mockValues).getBid())))
                        .subtract(new BigDecimal(Mocks.fees().getFee()));

        StepVerifier.create(calculator.addTargetPrice.apply(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> assertThat(trade.getTargetPrice(), is(solution)))
                .thenCancel()
                .verify();
    }

    @RepeatedTest(10)
    void shouldReturnTradeForShortStrategiesWithTargetPriceSet() {
        mockTrade.setCurrentPrice(new BigDecimal(price))
                .setType(StrategyType.SHORT)
                .setEntryPercentage(new BigDecimal(mockStrategy.getEntry()))
                .setFees(Mocks.fees());

        Flux<Trade> tradeFeedIn = Flux.just(mockTrade);

        BigDecimal solution =
                new BigDecimal(Mocks.krakenTickerMessage(mockValues).getAsk()).add(
                                (new BigDecimal(mockStrategy.getEntry()).movePointLeft(2))
                                        .multiply(new BigDecimal(Mocks.krakenTickerMessage(mockValues).getAsk())))
                        .add(new BigDecimal(Mocks.fees().getFee()));

        StepVerifier.create(calculator.addTargetPrice.apply(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> assertThat(trade.getTargetPrice(), is(solution)))
                .thenCancel()
                .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "101"})
    void shouldThrowExceptionIfTradeEntryIsOutOfBounds(String value) {
        mockTrade.setEntryPercentage(new BigDecimal(value));

        Flux<Trade> tradeFeedIn = Flux.just(mockTrade);

        StepVerifier.create(calculator.addTargetPrice.apply(tradeFeedIn))
                .expectSubscription()
                .expectErrorSatisfies(error -> {
                    assertThat(error, isA(OutOfBoundsException.class));
                    assertThat(error.getMessage(), is(String.format("%s is outside the range of 0-100", value)));
                })
                .verify();
    }
}
