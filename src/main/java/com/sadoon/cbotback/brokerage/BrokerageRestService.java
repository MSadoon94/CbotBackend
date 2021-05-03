package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BrokerageRestService {

    private final WebClient client;

    private int nonceCount;

    public BrokerageRestService(WebClient client) {
        this.client = client;
    }

    public Map<String, String> getBalance(KrakenRequest request) {
        request.setEndpoint("/0/private/Balance");

        nonceCount++;
        request.setNonce(new NonceCreator().createNonce(nonceCount));

        return client.post()
                .uri("/0/private/Balance")
                .attribute("request", request)
                .retrieve()
                .bodyToMono(LinkedHashMap.class)
                .block();
    }

}
