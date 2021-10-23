package com.sadoon.cbotback.brokerage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.util.BrokerageDto;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.KrakenRequestException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@JsonTest
class WebClientServiceTest {

    @Autowired
    private ObjectMapper objectMapper;

    static MockWebServer mockServer;

    private WebClientService webClientService;

    private Balances mockBalances = Mocks.balances("USD", "100");

    private BrokerageDto mockDto = Mocks.brokerageDTO("balance", mockServer.url("/").toString());

    private final ParameterizedTypeReference<Balances> balancesRef = new ParameterizedTypeReference<>() {
    };

    @BeforeAll
    static void setUpServer() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @BeforeEach
    public void setup() {
        webClientService = new WebClientService();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void shouldReturnResponseOfSpecifiedTypeOnSuccess() throws JsonProcessingException, KrakenRequestException {
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(objectMapper.writeValueAsString(mockBalances))
                .addHeader("Content-Type", "application/json");

        mockServer.enqueue(mockResponse);

        assertThat(webClientService.onResponse(balancesRef, mockDto).getBalances(),
                is(mockBalances.getBalances()));

    }

    @Test
    void shouldReturnWebClientExceptionOnFail() throws JsonProcessingException {
        mockBalances.unpackErrors(new String[]{"mockError"});
        MockResponse mockResponse = new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(objectMapper.writeValueAsString(mockBalances))
                .addHeader("Content-Type", "application/json");

        mockServer.enqueue(mockResponse);

        assertThrows(WebClientResponseException.class,
                () -> webClientService.onResponse(balancesRef, mockDto));
    }
}