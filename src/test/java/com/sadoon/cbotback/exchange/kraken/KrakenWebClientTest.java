package com.sadoon.cbotback.exchange.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.api.KrakenResponse;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.exchange.structure.ExchangeResponseHandler;
import com.sadoon.cbotback.security.credentials.SecurityCredentials;
import com.sadoon.cbotback.tools.Mocks;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class KrakenWebClientTest {
    private ObjectMapper mapper = new ObjectMapper();
    private static MockWebServer mockWebServer;

    @Mock
    private NonceCreator nonceCreator;
    @Mock
    private ExchangeResponseHandler responseHandler;
    private TradeVolume mockVolume;


    private String mockNonce = "mockNonce";
    private List<String> pairs = List.of("BTCUSD");
    private SecurityCredentials credentials
            = new SecurityCredentials(ExchangeType.KRAKEN.name(), "mockAccount", "mockPassword");

    private KrakenWebClient client;

    @BeforeEach
    public void setUp() throws IOException {
        mockVolume = Mocks.tradeVolume(mapper, new String[]{});
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new KrakenWebClient(
                responseHandler,
                nonceCreator,
                mockWebServer.url("/").toString()
        );

    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldSetApiSignToKrakenSpecifications() throws InterruptedException, JsonProcessingException {
        given(nonceCreator.createNonce()).willReturn(mockNonce);
        String signature = new SignatureCreator().signature(
                mockNonce.concat(formattedVolumeRequest()),
                credentials.password(),
                "/0/private/TradeVolume"
        );

        mockWebServer.enqueue(mockResponse(HttpStatus.OK, mockVolume));

        client.tradeVolume(credentials, pairs)
                .block();

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getHeaders().get("API-Sign"), is(signature));
    }

    @Test
    void shouldReturnTradeVolume() throws JsonProcessingException {
        given(nonceCreator.createNonce()).willReturn(mockNonce);
        mockWebServer.enqueue(mockResponse(HttpStatus.OK, mockVolume));

        assertThat(client.tradeVolume(credentials, pairs).block(),
                samePropertyValuesAs(mockVolume));
    }

    @Test
    void shouldReturnTradeWithAllNamesSet() throws JsonProcessingException {
        Trade mockTrade = Mocks.trade(TradeStatus.SELECTED, BigDecimal.ZERO, BigDecimal.ONE);
        mockTrade.setAllNames(new ArrayList<>(Collections.singleton(mockTrade.getPair())));
        Flux<Trade> tradeFeedIn = Flux.just(mockTrade);

        mockWebServer.enqueue(mockResponse(
                HttpStatus.OK,
                Mocks.assetPairs()
        ));

        StepVerifier.create(client.assetPairTradeFeed(tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> {
                    System.out.println(trade.getAllNames());
                    assertThat(trade.getAllNames(),
                            containsInAnyOrder(
                                    mockTrade.getPair(),
                                    Mocks.assetPair().getAltName(),
                                    Mocks.assetPair().getWsName()));
                })
                .thenCancel()
                .verify();
    }

    @Test
    void shouldReturnTradeWithFeesSet() throws JsonProcessingException, ExchangeRequestException {
        Flux<Trade> tradeFeedIn = Flux.just(Mocks.trade(TradeStatus.SELECTED, BigDecimal.ONE, BigDecimal.TEN));

        given(nonceCreator.createNonce()).willReturn(mockNonce);
        given(responseHandler.getFees(any())).willReturn(List.of(Mocks.fees()));

        mockWebServer.enqueue(mockResponse(
                HttpStatus.OK,
                mockVolume
        ));

        StepVerifier.create(client.tradeVolumeTradeFeed(credentials, tradeFeedIn))
                .expectSubscription()
                .consumeNextWith(trade -> assertThat(trade.getFees(), samePropertyValuesAs(Mocks.fees())))
                .thenCancel()
                .verify();
    }

    @Test
    void shouldThrowExceptionOnAssetPairRequestError() throws JsonProcessingException {
        mockWebServer.enqueue(mockResponse(HttpStatus.BAD_REQUEST, mockVolume));

        StepVerifier.create(client.assetPairs(pairs.get(0)))
                .expectSubscription()
                .expectErrorSatisfies(error -> {
                    assertThat(error, isA(ExchangeRequestException.class));
                    assertThat(error.getMessage(),
                            containsString("KRAKEN responded with: 400 Bad Request from GET"));
                })
                .verify();
    }

    @Test
    void shouldThrowExceptionOnTradeVolumeRequestError() throws JsonProcessingException {
        given(nonceCreator.createNonce()).willReturn(mockNonce);
        mockWebServer.enqueue(mockResponse(HttpStatus.BAD_REQUEST, mockVolume));

        StepVerifier.create(client.tradeVolume(credentials, pairs))
                .expectSubscription()
                .expectErrorSatisfies(error -> {
                    assertThat(error, isA(ExchangeRequestException.class));
                    assertThat(error.getMessage(),
                            containsString("KRAKEN responded with: 400 Bad Request from POST"));
                })
                .verify();
    }


    private String formattedVolumeRequest() {
        return Map.of(
                        "nonce", mockNonce,
                        "pair", String.join(",", pairs),
                        "fee-info", "true"
                )
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("nonce"))
                .map(entry -> String.format("%1s=%2s", entry.getKey(), entry.getValue()))
                .reduce(String.format("nonce=%1s", mockNonce),
                        (partialBody, entry) -> String.format("%1s&%2s", partialBody, entry));
    }

    private <T extends KrakenResponse> MockResponse mockResponse(HttpStatus status,
                                                                 T krakenResponse) throws JsonProcessingException {
        return new MockResponse()
                .setResponseCode(status.value())
                .setBody(mapper.writeValueAsString(krakenResponse))
                .addHeader("Content-Type", "application/json");
    }

}
