package com.sadoon.cbotback.exchange.kraken;

import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.exchange.model.Trade;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.exchange.structure.ExchangeResponseHandler;
import com.sadoon.cbotback.exchange.structure.ExchangeWebClient;
import com.sadoon.cbotback.security.credentials.SecurityCredentials;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class KrakenWebClient implements ExchangeWebClient {
    private WebClient client;
    private ExchangeResponseHandler responseHandler;
    private NonceCreator nonceCreator;
    private List<String> tradeVolumePairs = new ArrayList<>();

    public KrakenWebClient(ExchangeResponseHandler responseHandler,
                           NonceCreator nonceCreator,
                           String baseUrl) {
        this.responseHandler = responseHandler;
        this.nonceCreator = nonceCreator;
        client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Override
    public Flux<Trade> assetPairTradeFeed(Flux<Trade> tradeFeedIn) {
        return tradeFeedIn
                .flatMap(trade ->
                        assetPairs(trade.getPair())
                                .map(assetPairs -> assetPairs.getPairs().get(trade.getPair()))
                                .map(pair -> trade.addPairNames(List.of(pair.getAltName(), pair.getWsName()))));
    }

    @Override
    public Flux<Trade> tradeVolumeTradeFeed(SecurityCredentials credentials, Flux<Trade> tradeFeedIn) {
        return tradeFeedIn
                .doOnNext(trade -> {
                    if (!tradeVolumePairs.contains(trade.getPair())) {
                        tradeVolumePairs.add(trade.getPair());
                    }
                })
                .zipWith(getFees(tradeVolume(credentials, tradeVolumePairs)))
                .flatMap(tradeFeeTuple -> addFees(tradeFeeTuple.getT2(), tradeFeeTuple.getT1()));
    }

    private Flux<Map<String, Fees>> getFees(Mono<TradeVolume> tradeVolumeMono) {
        return tradeVolumeMono
                .log()
                .flux()
                .flatMap(tradeVolume -> Mono.fromCallable(() ->
                                responseHandler
                                        .getFees(tradeVolume))
                        .flux()
                        .flatMapIterable(Function.identity())
                        .map(fee -> Map.of(fee.getPair(), fee))
                        .subscribeOn(Schedulers.boundedElastic()));
    }


    private Mono<Trade> addFees(Map<String, Fees> fees, Trade trade) {
        return Mono.fromCallable(() -> trade.setFees(fees.get(trade
                        .getAllNames()
                        .stream()
                        .filter(fees::containsKey)
                        .findFirst()
                        .get())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<AssetPairs> assetPairs(String pairs) {
        return client.get()
                .uri(String.format("/0/public/AssetPairs?=%1s", pairs))
                .retrieve()
                .bodyToMono(AssetPairs.class)
                .onErrorResume(error -> Mono.error(
                        new ExchangeRequestException(ExchangeType.KRAKEN, error.getMessage())));
    }

    @Override
    public Mono<TradeVolume> tradeVolume(SecurityCredentials credentials, List<String> pairs) {
        String endpoint = "/0/private/TradeVolume";
        Map<String, String> bodyValues = tradeVolumeRequest(nonceCreator.createNonce(), pairs);

        return getHeaders(client.method(HttpMethod.POST), credentials, bodyValues, endpoint)
                .bodyValue(formatBodyValues(bodyValues))
                .retrieve()
                .bodyToMono(TradeVolume.class)
                .onErrorResume(error -> Mono.error(
                        new ExchangeRequestException(ExchangeType.KRAKEN, error.getMessage())));
    }

    private WebClient.RequestBodySpec getHeaders(WebClient.RequestBodyUriSpec bodySpec,
                                                 SecurityCredentials credentials,
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

    private String getSignature(SecurityCredentials credentials,
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