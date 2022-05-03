package com.sadoon.cbotback.exchange.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.Configuration;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.tools.TestMessageChannel;
import com.sadoon.cbotback.tools.WebSocketTest;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.trade.TradeController;
import com.sadoon.cbotback.trade.TradeListener;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private TradeListener tradeListener;
    @Mock
    private ExchangeSupplier exchangeSupplier;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    private Trade mockTrade;

    private TradeController tradeController;

    private WebSocketTest webSocketTest;

    @BeforeEach
    void setUp() {
        tradeController = new TradeController(tradeListener, exchangeSupplier, userService);
        webSocketTest = new WebSocketTest(tradeController, new SimpMessagingTemplate(new TestMessageChannel()));
        mockTrade = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.TEN)
                .setLabel("KRAKEN1BTC/USD100Long");
        mockUser.setTrades(Map.of(mockTrade.getId(), mockTrade));
    }

    @Test
    void shouldSendTradesToSubscribersOnSubscribe() throws UserNotFoundException {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        webSocketTest.sendMessageToController(
                webSocketTest.subscribeHeaderAccessor("/topic/trades", auth)
        );

        Message<?> reply = webSocketTest.getOutboundChannel().getMessages().get(0);
        String responseJson = new String((byte[]) reply.getPayload(), StandardCharsets.UTF_8);

        Map<String, Object> trades =
                (Map<String, Object>) Configuration.defaultConfiguration().jsonProvider().parse(responseJson);
        Trade trade = Mocks.mapper.convertValue(trades.get(mockTrade.getId().toString()), Trade.class);


        assertThat(trade, samePropertyValuesAs(mockTrade, "fees", "id"));
        assertThat(trade.getFees(), samePropertyValuesAs(mockTrade.getFees(), "pair"));
    }

    @Test
    void shouldReturnTradeOnCreateTradeSuccess() throws UserNotFoundException, JsonProcessingException {
        Strategy mockStrategy = Mocks.strategy();

        mockUser.setStrategies(Map.of(Mocks.strategy().getName(), mockStrategy));
        Trade expectedTrade = expectedTrade(mockStrategy);
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        webSocketTest.sendMessageToController(
                webSocketTest.sendHeaderAccessor(
                        String.format("/app/create-trade", mockStrategy.getName()), auth),
                Mocks.mapper.writeValueAsBytes(Map.of("name", mockStrategy.getName()))
        );

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);
        Map<UUID, Trade> result = (Map<UUID, Trade>) reply.getPayload();
        assertThat(result.get(mockTrade.getId()), samePropertyValuesAs(expectedTrade, "id", "fees"));
    }

    private Trade expectedTrade(Strategy mockStrategy) {
        List<String> allNames = new ArrayList<>(List.of("BTC/USD", "XBTUSD"));
        return new Trade()
                .setStatus(TradeStatus.SELECTED)
                .setLabel("KRAKEN1BTC/USD100Long")
                .setTargetPrice(BigDecimal.TEN)
                .setExchange(ExchangeName.valueOf(mockStrategy.getExchange().toUpperCase()))
                .setAllNames(allNames)
                .setCurrentPrice(BigDecimal.ONE)
                .setPair(mockStrategy.getPair())
                .setType(StrategyType.valueOf(mockStrategy.getType().toUpperCase()))
                .setEntryPercentage(BigDecimal.TEN);
    }

}
