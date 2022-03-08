package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exceptions.notfound.StrategyTypeNotFoundException;
import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.websocket.TickerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PriceCalculatorTest {

    @Mock
    private AssetTracker tracker;

    private PriceCalculator calculator;

    private Strategy mockStrategy = Mocks.strategy();
    private String entry = String.valueOf(Math.random());
    private String price = String.valueOf(Math.floor(Math.random() * ((1000000 - 1 + 1) + 1)));

    @BeforeEach
    public void setUp() {
        mockStrategy.setBrokerage("mockBrokerage");
        calculator = new PriceCalculator(Map.of(mockStrategy.getBrokerage(), tracker));
    }

    @RepeatedTest(10)
    void shouldReturnTargetPriceAsBigDecimalForLongStrategies() throws StrategyTypeNotFoundException, OutOfBoundsException {
        mockStrategy.setEntry(entry);
        mockStrategy.setType(StrategyType.LONG.name());
        String[] mockValues = new String[]{price, "1", "1"};
        Flux<Mono<? extends TickerMessage>> tickerFlux
                = Flux.just(Mono.just(Mocks.krakenTickerMessage(mockValues)));
        given(tracker.getTickerFlux(any())).willReturn(tickerFlux);


        BigDecimal solution =
                (new BigDecimal(mockStrategy.getEntry()).movePointLeft(2))
                        .multiply(new BigDecimal(Mocks.krakenTickerMessage(mockValues).getBid()));

        assertThat(calculator.targetPrice(mockStrategy), is(solution));
    }

    @RepeatedTest(10)
    void shouldReturnTargetPriceAsBigDecimalForShortStrategies() throws StrategyTypeNotFoundException, OutOfBoundsException {
        mockStrategy.setEntry(entry);
        mockStrategy.setType(StrategyType.SHORT.name());
        String[] mockValues = new String[]{price, "1", "1"};

        Flux<Mono<? extends TickerMessage>> tickerFlux
                = Flux.just(Mono.just(Mocks.krakenTickerMessage(mockValues)));
        given(tracker.getTickerFlux(any())).willReturn(tickerFlux);


        BigDecimal solution =
                (new BigDecimal(mockStrategy.getEntry()).movePointLeft(2))
                        .multiply(new BigDecimal(Mocks.krakenTickerMessage(mockValues).getAsk()));

        assertThat(calculator.targetPrice(mockStrategy), is(solution));
    }

    @Test
    void shouldThrowExceptionIfStrategyTypeNotFound() {
        mockStrategy.setEntry(entry);
        mockStrategy.setType("unknown");
        String[] mockValues = new String[]{price, "1", "1"};
        Flux<Mono<? extends TickerMessage>> tickerFlux
                = Flux.just(Mono.just(Mocks.krakenTickerMessage(mockValues)));
        given(tracker.getTickerFlux(any())).willReturn(tickerFlux);

        assertThrows(StrategyTypeNotFoundException.class, () -> calculator.targetPrice(mockStrategy));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "101"})
    void shouldThrowExceptionIfStrategyEntryIsOutOfBounds(String value) {
        mockStrategy.setEntry(value);
        mockStrategy.setType(StrategyType.LONG.name());

        assertThrows(OutOfBoundsException.class, () -> calculator.targetPrice(mockStrategy));
    }


}
