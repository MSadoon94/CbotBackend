package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.meta.TradeStatus;
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
    void shouldCreateTradeId(){
        Trade targetValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ONE);
        Flux<Trade> tradeFeedIn = Flux.just(targetValue);

        String targetId = String.join("",
                targetValue.getExchange().name(),
                targetValue.getCurrentPrice().toString(),
                targetValue.getPair(),
                targetValue.getTargetPrice().toString(),
                targetValue.getType().name()
        );

        StepVerifier.create(scanner.tradeFeed(tradeFeedIn))
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

        StepVerifier.create(scanner.tradeFeed(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> tradeAssert(trade, lesserValue.getCurrentPrice(), lesserValue.getStatus()))
                .consumeNextWith(trade -> tradeAssert(trade, targetValue.getCurrentPrice(), targetValue.getStatus()))
                .consumeNextWith(trade -> tradeAssert(trade, greaterValue.getCurrentPrice(), greaterValue.getStatus()))
                .thenCancel()
                .verify();
    }



    @Test
    void shouldReturnTradeFeedWithShortTradesThatAreInTargetRange() {
        Trade lesserValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ZERO, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade targetValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.ONE).setType(StrategyType.SHORT);
        Trade greaterValue = Mocks.trade(TradeStatus.SELECTED, BigDecimal.TEN, BigDecimal.ONE).setType(StrategyType.SHORT);
        Flux<Trade> tradeFeedIn = Flux.just(lesserValue, targetValue, greaterValue);

        StepVerifier.create(scanner.tradeFeed(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> tradeAssert(trade, lesserValue.getCurrentPrice(), lesserValue.getStatus()))
                .consumeNextWith(trade -> tradeAssert(trade, targetValue.getCurrentPrice(), targetValue.getStatus()))
                .consumeNextWith(trade -> tradeAssert(trade, greaterValue.getCurrentPrice(), greaterValue.getStatus()))
                .thenCancel()
                .verify();
    }

    private void tradeAssert(Trade trade, BigDecimal expectedPrice, TradeStatus expectedStatus){
        assertThat(trade.getCurrentPrice(), is(expectedPrice));
        assertThat(trade.getStatus(), is(expectedStatus));
    }

}
