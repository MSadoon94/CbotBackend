package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exchange.Exchange;
import com.sadoon.cbotback.exp.Trade;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EntryScannerTest {

    private EntryScanner scanner;
    private Exchange exchange;
    private ExecutorConfig executorConfig = new ExecutorConfig();
    @Mock
    private AssetTracker tracker;
    @MockBean
    private Logger logger;

    private String[] lesserValue = new String[]{"1000", "1", "1"};
    private String[] targetValue = new String[]{"1001", "1", "1"};
    private String[] greaterValue = new String[]{"1002", "1", "1"};

    @BeforeEach
    public void setUp() {
        exchange = new Exchange()
                .setTracker(tracker);
        scanner = new EntryScanner(exchange, executorConfig.entryScannerExecutor());
    }

    @AfterEach
    public void shutdown() {
        executorConfig.shutdown(logger);
    }

    @Test
    void shouldUpdateSubscribersWhenPotentialLongEntryFound() {


        given(tracker.getTickerFlux(any()))
                .willReturn(Flux.just(
                        Mono.just(Mocks.krakenTickerMessage(lesserValue)),
                        Mono.just(Mocks.krakenTickerMessage(targetValue)),
                        Mono.just(Mocks.krakenTickerMessage(greaterValue))
                ));

        Trade trade = new Trade()
                .setTargetPrice(new BigDecimal("1001"))
                .setPair("BTC/USD")
                .setType(StrategyType.LONG);

        StepVerifier.create(scanner.getPotentialTrades())
                .expectSubscription()
                .then(() -> scanner.addTradesToScan(List.of(trade)))
                .consumeNextWith(message -> assertThat(message.getCurrentPrice(), is(new BigDecimal(targetValue[0]))))
                .consumeNextWith(message -> assertThat(message.getCurrentPrice(), is(new BigDecimal(greaterValue[0]))))
                .thenCancel()
                .verify();

    }

    @Test
    void shouldUpdateSubscribersWhenPotentialShortEntryFound() {

        given(tracker.getTickerFlux(any()))
                .willReturn(Flux.just(
                        Mono.just(Mocks.krakenTickerMessage(greaterValue)),
                        Mono.just(Mocks.krakenTickerMessage(targetValue)),
                        Mono.just(Mocks.krakenTickerMessage(lesserValue))
                ));

        Trade trade = new Trade()
                .setTargetPrice(new BigDecimal("1001"))
                .setPair("BTC/USD")
                .setType(StrategyType.SHORT);

        StepVerifier.create(scanner.getPotentialTrades())
                .expectSubscription()
                .then(() -> scanner.addTradesToScan(List.of(trade)))
                .consumeNextWith(message -> assertThat(message.getCurrentPrice(), is(new BigDecimal(targetValue[0]))))
                .consumeNextWith(message -> assertThat(message.getCurrentPrice(), is(new BigDecimal(lesserValue[0]))))
                .thenCancel()
                .verify();
    }

}
