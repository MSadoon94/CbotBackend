package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CandleProcessorTest {

    private CandleProcessor processor;

    @BeforeEach
    public void setUp() {
        processor = new CandleProcessor();
    }

    @Test
    void shouldReturnCandleFeedOnSuccess() {
        Flux<TickerMessage> tickerFeed = Flux.just(
                Mocks.krakenTickerMessage(new String[]{"10"}),
                Mocks.krakenTickerMessage(new String[]{"100"}),
                Mocks.krakenTickerMessage(new String[]{"1"}),
                Mocks.krakenTickerMessage(new String[]{"9"})
        );

        StepVerifier.create(processor.toCandles(StrategyType.LONG, Duration.ofMillis(100)).apply(tickerFeed))
                .consumeNextWith(candle -> {
                    assertThat(candle.open(), is("10"));
                    assertThat(candle.high(), is("100"));
                    assertThat(candle.low(), is("1"));
                    assertThat(candle.close(), is("9"));
                })
                .thenCancel()
                .verify();
    }
}
