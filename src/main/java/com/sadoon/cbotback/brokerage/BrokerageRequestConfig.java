package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

public class BrokerageRequestConfig {


    public WebClient webClient(String brokerageUrl) {
        return WebClient.builder()
                .baseUrl(brokerageUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .filter(headerFilter)
                .filter(bodyFilter)
                .build();
    }

    private ExchangeFilterFunction headerFilter = ((clientRequest, nextFilter) -> {
        ClientRequest request;
        if (clientRequest.url().toString().contains("public")) {
            request = ClientRequest.from(clientRequest)
                    .build();
        } else {
            request = ClientRequest.from(clientRequest)
                    .header("API-Key", getKrakenRequest(clientRequest).getAccount())
                    .header("API-Sign", getSignature(getKrakenRequest(clientRequest)))
                    .build();
        }
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
