package com.sadoon.cbotback.exchange.structure;

import com.sadoon.cbotback.exceptions.exchange.ExchangeConnectionException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.tools.TestMessageChannel;
import com.sadoon.cbotback.tools.WebSocketTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExchangeUtilMessageProcessorTest {

    @Mock
    private ExchangeWebSocket socket;
    @Mock
    private ExchangeMessageHandler messageHandler;

    private SimpMessagingTemplate messagingTemplate = new SimpMessagingTemplate(new TestMessageChannel());

    private WebSocketTest webSocketTest;

    private ExchangeMessageProcessor processor;

    private List<String> values = List.of("mock1", "mock2");

    @BeforeEach
    public void setUp() {
        processor = new ExchangeMessageProcessor(socket, messageHandler, messagingTemplate);
        webSocketTest = new WebSocketTest(processor, messagingTemplate);

    }

    private Function<Flux<String>, Flux<String>> toUpperCase = (messageFeed) -> messageFeed.map(String::toUpperCase);

    @Test
    void shouldApplyTransformationToInput() {
        given(messageHandler.getMessageFeed()).willReturn(Flux.fromIterable(values));

        StepVerifier.create(processor.convertMessages(toUpperCase))
                .consumeNextWith(message -> assertThat(message, is(values.get(0).toUpperCase())))
                .consumeNextWith(message -> assertThat(message, is(values.get(1).toUpperCase())))
                .thenCancel()
                .verify();
    }

    @Test
    void shouldSendMessageToBroker() {
        given(messageHandler.getMessageFeed()).willReturn(Flux.fromIterable(values));

        processor.convertAndSendUpdates(toUpperCase, "/topic/price/kraken/btcusd");

        List<Message<?>> replies = webSocketTest.getBrokerMessagingChannel().getMessages();

        assertThat(replies.get(0).getPayload(), is(values.get(0).toUpperCase()));
        assertThat(replies.get(1).getPayload(), is(values.get(1).toUpperCase()));
    }

    @Test
    void shouldCompleteOnSendMessageSuccess() {
        given(messageHandler.sendMessage(any())).willReturn(Sinks.EmitResult.OK);

        StepVerifier.create(processor.sendMessage(Mono.just("message")))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldThrowExceptionOnSendMessageError() {
        given(messageHandler.sendMessage(any())).willReturn(Sinks.EmitResult.FAIL_TERMINATED);

        StepVerifier.create(processor.sendMessage(Mono.just("message")))
                .expectErrorSatisfies(error -> {
                    assertThat(error, isA(ExchangeConnectionException.class));
                    assertThat(error.getMessage(),
                            is(String.format("Could not send message to %1s due to: %2s",
                                    ExchangeName.KRAKEN, Sinks.EmitResult.FAIL_TERMINATED)));
                })
                .verify();
    }

}
