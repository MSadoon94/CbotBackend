package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.exchange.structure.ExchangeSupplier;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.tools.TestMessageChannel;
import com.sadoon.cbotback.tools.WebSocketTest;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @Mock
    private KrakenWebClient webClient;
    @Mock
    private ExchangeSupplier supplier;

    private WebSocketTest webSocketTest;

    private Exchange exchange;

    private AssetController controller;

    private AssetPairMessage mockMessage = Mocks.assetPairMessage();
    private AssetPairs mockAssetPairs = Mocks.assetPairs();

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    @BeforeEach
    void setUp() {
        exchange = new Exchange()
                .setWebClient(webClient);
        given(supplier.getExchange(any())).willReturn(exchange);
        controller = new AssetController(supplier);
        webSocketTest = new WebSocketTest(controller, new SimpMessagingTemplate(new TestMessageChannel()));
    }

    @Test
    void shouldSendAssetPairsToSubscribersOnSuccess() throws Exception {
        given(webClient.assetPairs(anyString())).willReturn(Mono.just(mockAssetPairs));

        webSocketTest.sendMessageToController(
                webSocketTest.sendHeaderAccessor("/app/asset-pairs", auth),
                Mocks.mapper.writeValueAsBytes(mockMessage));

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);

        assertThat(reply.getPayload(), samePropertyValuesAs(mockAssetPairs));
    }
}
