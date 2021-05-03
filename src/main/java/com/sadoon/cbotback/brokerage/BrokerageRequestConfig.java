package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BrokerageRequestConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.kraken.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .filter(headerFilter)
                .filter(bodyFilter)
                .build();
    }

    private ExchangeFilterFunction headerFilter = ((clientRequest, nextFilter) -> {
        ClientRequest request = ClientRequest.from(clientRequest)
                .header("API-Key", getKrakenRequest(clientRequest).getAccount())
                .header("API-Sign", getSignature(getKrakenRequest(clientRequest)))
                .build();
        return nextFilter.exchange(request);

    });

    private ExchangeFilterFunction bodyFilter = ((clientRequest, nextFilter) -> {

        ClientRequest request = ClientRequest.from(clientRequest)
                .body(BodyInserters.fromFormData(getKrakenRequest(clientRequest).getBodyValues()))
                .build();

        return nextFilter.exchange(request);

    });


    private String getSignature(KrakenRequest request) {
        return new SignatureCreator().getSignature(request);
    }

    private KrakenRequest getKrakenRequest(ClientRequest clientRequest) {
        return (KrakenRequest) clientRequest.attribute("request").orElse(null);
    }


}
