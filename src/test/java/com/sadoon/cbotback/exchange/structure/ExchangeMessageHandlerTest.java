package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExchangeMessageHandlerTest {

    @Mock
    private WebSocketSession session;

    private ExchangeMessageHandler messageHandler;

    @BeforeEach
    public void setUp() {
        messageHandler = new ExchangeMessageHandler();
    }

    @Test
    void shouldEmitMessageAsStringOnIncomingMessage() {
        List<String> values = List.of("mock1", "mock2");
        Flux<WebSocketMessage> messageFeed = Mocks.mockMessageFlux(values);
        given(session.receive()).willReturn(messageFeed);

        messageHandler.handle(session)
                .subscribe();

        StepVerifier.create(messageHandler.getMessageFeed())
                .consumeNextWith(message -> assertThat(message, is(values.get(0))))
                .consumeNextWith(message -> assertThat(message, is(values.get(1))))
                .thenCancel()
                .verify();
    }

}
