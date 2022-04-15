package com.sadoon.cbotback.exchange.structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.tools.TestMessageChannel;
import com.sadoon.cbotback.tools.WebSocketTest;
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
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class TradeControllerTest {

    @Mock
    private UserService userService;
    private SimpMessagingTemplate messagingTemplate = new SimpMessagingTemplate(new TestMessageChannel());


    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    private Trade mockTrade;

    private TradeController tradeController;

    private WebSocketTest webSocketTest;

    @BeforeEach
    void setUp() {
        tradeController = new TradeController(messagingTemplate, userService);
        webSocketTest = new WebSocketTest(tradeController, messagingTemplate);
        mockTrade = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.TEN)
                .setID("KRAKEN1BTC/USD100Long");
        mockUser.setTrades(Map.of(mockTrade.getPair(), mockTrade));
    }

    @Test
    void shouldSendTradeToSubscribersOnSubscribe() throws UserNotFoundException, JsonProcessingException {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        given(userService.getTradeFeed(any())).willReturn(Flux.just(mockTrade));

        webSocketTest.responseMessage(
                webSocketTest.subscribeHeaderAccessor("/topic/trade-feed", auth),
                new ObjectMapper().writeValueAsBytes(mockTrade)
        );

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);

        assertThat(reply.getPayload(), is(mockTrade));
    }

}
