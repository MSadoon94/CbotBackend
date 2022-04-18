package com.sadoon.cbotback.exchange.structure;

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
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExchangeMessageProcessorTest {

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

}
