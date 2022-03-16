package com.sadoon.cbotback.exchange;

import com.sadoon.cbotback.exchange.model.PayloadType;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WebSocketFunctionsTest {

    @Mock
    private WebSocketSession session;

    private WebSocketFunctions functions;

    @BeforeEach
    public void setUp() {
        functions = new WebSocketFunctions();

    }

    @Test
    void shouldReturnFluxWithTargetPayloadTypes() {
        given(session.receive()).willReturn(Mocks.mockMessageFlux(List.of(
                PayloadType.TICKER.name().toLowerCase(),
                "",
                PayloadType.UNKNOWN.name().toLowerCase(),
                PayloadType.TICKER.name().toLowerCase())
        ));
        Function<WebSocketSession, Flux<WebSocketMessage>> function =
                functions.receiveMessages(
                        new BaseSubscriber<WebSocketMessage>() {
                        },
                        Mocks.mockTickerMessageFilter()
                );

        StepVerifier.create(function.apply(session))
                .expectSubscription()
                .consumeNextWith(message -> testPayload(message, PayloadType.TICKER.name().toLowerCase()))
                .consumeNextWith(message -> testPayload(message, PayloadType.TICKER.name().toLowerCase()))
                .verifyComplete();
    }

    private void testPayload(WebSocketMessage payload, String expectation) {
        assertThat(payload.getPayloadAsText(), is(expectation));
    }

}