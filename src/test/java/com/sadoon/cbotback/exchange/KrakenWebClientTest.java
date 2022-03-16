package com.sadoon.cbotback.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.api.KrakenResponse;
import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exchange.model.ExchangeCredentials;
import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.tools.Mocks;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class KrakenWebClientTest {
    private ObjectMapper mapper = new ObjectMapper();
    private static MockWebServer mockWebServer;

    @Mock
    private NonceCreator nonceCreator;

    private String mockNonce = "mockNonce";
    private String pair = "BTCUSD";
    private ExchangeCredentials credentials
            = new ExchangeCredentials("mockAccount", "mockPassword");

    private KrakenWebClient client;


    @BeforeAll
    static void setUpMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownMockServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void setUp() {
        client = new KrakenWebClient(
                nonceCreator,
                mockWebServer.url("/").toString()
        );

    }

    @Test
    void shouldSetApiSignToKrakenSpecifications() throws InterruptedException, JsonProcessingException {
        given(nonceCreator.createNonce()).willReturn(mockNonce);
        String signature = new SignatureCreator().signature(
                mockNonce.concat(formattedVolumeRequest()),
                credentials.password(),
                "/0/private/TradeVolume"
        );

        mockWebServer.enqueue(mockResponse(HttpStatus.OK, Mocks.mockTradeVolume(new String[]{})));

        client.getTradeVolume(credentials, pair)
                .block();

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getHeaders().get("API-Sign"), is(signature));
    }

    @Test
    void shouldReturnTradeVolume() throws JsonProcessingException {
        given(nonceCreator.createNonce()).willReturn(mockNonce);
        mockWebServer.enqueue(mockResponse(HttpStatus.OK, Mocks.mockTradeVolume(new String[]{})));

        assertThat(client.getTradeVolume(credentials, pair).block(), samePropertyValuesAs(Mocks.mockTradeVolume(new String[]{})));
    }

    @Test
    void shouldReturnMonoOnTradeVolumeError() throws JsonProcessingException {
        given(nonceCreator.createNonce()).willReturn(mockNonce);
        mockWebServer.enqueue(mockResponse(HttpStatus.BAD_REQUEST, Mocks.mockTradeVolume(new String[]{})));

        assertThrows(WebClientResponseException.class, () -> client.getTradeVolume(credentials, pair).block());
    }

    @Test
    void shouldReturnAssetPairs() throws JsonProcessingException {
        String pair = "BTC/USD";
        mockWebServer.enqueue(mockResponse(
                HttpStatus.OK,
                Mocks.assetPairs()
        ));

        AssetPairs assetPairs = client.getAssetPairs(pair).block();

        assertThat(assetPairs, samePropertyValuesAs(Mocks.assetPairs(), "pairs"));

        assertThat(assetPairs.getPairs().get(pair),
                samePropertyValuesAs(
                        Mocks.assetPairs().getPairs().get(pair),
                        "feeSchedule", "makerTakerFees")
        );

    }

    private String formattedVolumeRequest() {
        return Map.of(
                        "nonce", mockNonce,
                        "pair", String.join(",", pair),
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
