package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageFactory;
import com.sadoon.cbotback.exchange.structure.ExchangeMessageProcessor;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.trade.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssetTrackerTest {

    @Mock
    private ExchangeMessageFactory mockMessageFactory;
    @Mock
    private ExchangeMessageProcessor mockMessageHandler;

    private Flux<Trade> tradeFlux;
    private TickerMessage tickerMessage;

    private AssetTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = new AssetTracker(mockMessageFactory, mockMessageHandler);
        setUpMocks();
    }

    @Test
    void shouldReturnTradeWithCurrentPriceAdded() {
        StepVerifier.create(tracker.addCurrentPrice.apply(tradeFlux), StepVerifierOptions.create().checkUnderRequesting(true))
                .consumeNextWith(trade ->
                        assertThat(trade.getCurrentPrice(),
                                is(new BigDecimal(tickerMessage.getPrice(StrategyType.LONG)))))
                .thenCancel()
                .verify();
    }

    private void setUpMocks() {
        tradeFlux = Flux.just(
                Mocks.trade(null, null, null)
                        .setExchange(ExchangeName.KRAKEN)
                        .setPair("BTC/USD")
                        .setType(StrategyType.LONG)
        );
        tickerMessage = Mocks.krakenTickerMessage(new String[]{"10"});

        given(mockMessageHandler.sendMessage(any())).willReturn(Mono.empty());
        given(mockMessageFactory.tickerSubscribe(any())).willReturn(Mono.just("MockTickerSubscribeMessage"));
        given(mockMessageHandler.convertAndSendUpdates(any(), any()))
                .willReturn(Flux.just(tickerMessage.getPrice(StrategyType.LONG)));
    }
}