package com.sadoon.cbotback.exchange.kraken;

import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exchange.ExchangeWebClient;
import com.sadoon.cbotback.exchange.model.ExchangeCredentials;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KrakenWebClient implements ExchangeWebClient {
    private WebClient client;
    private NonceCreator nonceCreator;
    private List<String> pairs = new ArrayList<>();

    public KrakenWebClient(NonceCreator nonceCreator, String baseUrl) {
        this.nonceCreator = nonceCreator;
        client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Override
    public Mono<AssetPairs> getAssetPairs(String pair) {
        if (!pairs.contains(pair)) {
            pairs.add(pair);
        }
        return client.get()
                .uri(String.format("/0/public/AssetPairs?pair=%1s", String.join(",", pairs)))
                .retrieve()
                .bodyToMono(AssetPairs.class)
                .onErrorResume(Mono::error);
    }

    @Override
    public Mono<TradeVolume> getTradeVolume(ExchangeCredentials credentials, String pair) {
        if (!pairs.contains(pair)) {
            pairs.add(pair);
        }
        String endpoint = "/0/private/TradeVolume";
        Map<String, String> bodyValues = tradeVolumeRequest(nonceCreator.createNonce(), pairs);

        return getHeaders(client.method(HttpMethod.POST), credentials, bodyValues, endpoint)
                .bodyValue(formatBodyValues(bodyValues))
                .retrieve()
                .bodyToMono(TradeVolume.class)
                .onErrorResume(Mono::error);
    }

    private WebClient.RequestBodySpec getHeaders(WebClient.RequestBodyUriSpec bodySpec,
                                                 ExchangeCredentials credentials,
                                                 Map<String, String> bodyValues,
                                                 String endpoint) {

        return bodySpec.uri(endpoint)
                .header("API-Key", credentials.account().trim())
                .header("API-Sign",
                        getSignature(
                                credentials,
                                bodyValues,
                                endpoint
                        ));
    }

    private String getSignature(ExchangeCredentials credentials,
                                Map<String, String> bodyValues,
                                String endpoint) {

        return new SignatureCreator().signature(
                bodyValues.get("nonce").concat(formatBodyValues(bodyValues)),
                credentials.password(),
                endpoint
        );
    }

    private String formatBodyValues(Map<String, String> bodyValues) {
        String nonce = String.format("nonce=%1s", bodyValues.get("nonce"));

        return bodyValues.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("nonce"))
                .map(entry -> String.format("%1s=%2s", entry.getKey(), entry.getValue()))
                .reduce(nonce, (partialBody, entry) -> String.format("%1s&%2s", partialBody, entry));

    }

    private Map<String, String> tradeVolumeRequest(String nonce, List<String> pairs) {
        return Map.of(
                "nonce", nonce,
                "pair", String.join(",", pairs),
                "fee-info", "true"
        );
    }
}