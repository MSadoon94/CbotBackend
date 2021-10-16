package com.sadoon.cbotback.brokerage.util;

import com.sadoon.cbotback.common.PublicRequestDto;
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
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(publicHeaderFilter)
                .build();
    }

    private ExchangeFilterFunction bodyFilter = ((clientRequest, nextFilter) -> {

        ClientRequest request = ClientRequest.from(clientRequest)
                .body(BodyInserters.fromFormData(getApiRequest(clientRequest).getBodyValues()))
                .build();

        return nextFilter.exchange(request);

    });

    private ExchangeFilterFunction apiHeaderFilter = ((clientRequest, nextFilter) -> {
        ClientRequest request;

        request = ClientRequest.from(clientRequest)
                .header("API-Key", getApiRequest(clientRequest).getAccount())
                .header("API-Sign", getSignature(getApiRequest(clientRequest)))
                .build();
        return nextFilter.filter(bodyFilter).exchange(request);
    });


    private ExchangeFilterFunction publicHeaderFilter = ((clientRequest, nextFilter) -> {
        ClientRequest request;
        if (clientRequest.url().toString().contains("public")) {
            request = ClientRequest.from(clientRequest)
                    .build();
            return nextFilter.exchange(request);
        } else {
            return nextFilter.filter(apiHeaderFilter).exchange(clientRequest);
        }
    });


    private String getSignature(BrokerageDto request) {
        return new SignatureCreator().getSignature(request);
    }

    private BrokerageDto getApiRequest(ClientRequest clientRequest) {
        return (BrokerageDto) clientRequest.attribute("request").orElse(null);
    }

    private PublicRequestDto<?> getPublicRequest(ClientRequest clientRequest) {
        return (PublicRequestDto<?>) clientRequest.attribute("request").orElse(null);
    }

}
