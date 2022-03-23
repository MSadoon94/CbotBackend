package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.asset.AssetTracker;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exceptions.notfound.EntityNotFoundException;
import com.sadoon.cbotback.exceptions.outofbounds.OutOfBoundsException;
import com.sadoon.cbotback.exchange.Exchange;
import com.sadoon.cbotback.exchange.ExchangeWebClient;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.exp.Trade;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class TradeStarterTest {

    @Mock
    private UserService userService;
    @Mock
    private PriceCalculator priceCalculator;
    @Mock
    private ExchangeWebClient webClient;
    @Mock
    private FeeProcessor feeProcessor;
    @Mock
    private AssetTracker tracker;

    private User mockUser = Mocks.user();
    private Authentication mockAuth = Mocks.auth(mockUser);

    private Exchange exchange;
    private TradeStarter starter;

    @BeforeEach
    public void setUp() throws ExchangeRequestException, EntityNotFoundException {
        exchange = new Exchange()
                .setWebClient(webClient)
                .setTracker(tracker);
        given(userService.cbotStatusFlux()).willReturn(Flux.just(Mocks.cbotStatus()));
        starter = new TradeStarter(
                userService,
                priceCalculator,
                exchange,
                feeProcessor
        );
    }

    @Test
    void shouldReturnTradeWhenCbotStatusIsActive() throws EntityNotFoundException, OutOfBoundsException, ExchangeRequestException {
        TickerMessage tickerMessage =
                Mocks.krakenTickerMessage(new String[]{"1000", "1", "1"});
        Strategy strategy = Mocks.strategy();
        mockUser.setStrategies(Map.of(strategy.getPair(), strategy));
        BigDecimal target = new BigDecimal("1001");

        given(feeProcessor.fees(any())).willReturn(Mocks.mockFees());
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        given(priceCalculator.targetPrice(any(), any(), any()))
                .willReturn(target);
        given(webClient.getAssetPairs(any()))
                .willReturn(Mono.just(Mocks.assetPairs()));
        given(tracker.trackPair(any())).willReturn(tracker);
        given(tracker.getTickerFeed()).willReturn(Flux.just(tickerMessage));

        Trade targetTrade = new Trade()
                .setTargetPrice(target)
                .setCurrentPrice(new BigDecimal(tickerMessage.getPrice(StrategyType.LONG)))
                .setType(StrategyType.LONG)
                .setPair(strategy.getPair());

        StepVerifier.create(starter.processTrades(mockAuth).getTradeFeed())
                .expectSubscription()
                .assertNext(trade ->
                        assertThat(
                                trade,
                                samePropertyValuesAs(targetTrade, "currentPrice")
                        ))
                .thenCancel()
                .verify(Duration.ofSeconds(1L));
    }


}