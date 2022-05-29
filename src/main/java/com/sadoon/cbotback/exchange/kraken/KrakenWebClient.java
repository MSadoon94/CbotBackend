package com.sadoon.cbotback.exchange.kraken;

import com.sadoon.cbotback.asset.AssetPair;
import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.model.Balances;
import com.sadoon.cbotback.exchange.model.Fees;
import com.sadoon.cbotback.exchange.model.TradeVolume;
import com.sadoon.cbotback.exchange.structure.ExchangeResponseHandler;
import com.sadoon.cbotback.exchange.structure.ExchangeWebClient;
import com.sadoon.cbotback.security.credentials.SecurityCredential;
import com.sadoon.cbotback.security.util.NonceCreator;
import com.sadoon.cbotback.security.util.SignatureCreator;
import com.sadoon.cbotback.trade.Trade;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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

    private List<String> getPairNames(AssetPair pair) {
        int altNameLength = pair.getAltName().length();
        return List.of(pair.getAltName(),
                pair.getWsName(),
                String.format("X%1sZ%2s", pair.getBase(), pair.getQuote()),
                String.format("X%1sZ%2s",
                        pair.getAltName().substring(0, altNameLength / 2),
                        pair.getAltName().substring(altNameLength / 2))
        );
    }

    @Override
    public UnaryOperator<Flux<Trade>> addAllPairNames() {
        return tradeFeedIn -> tradeFeedIn
                .flatMap(trade ->
                        assetPairs(trade.getPair())
                                .map(assetPairs -> assetPairs.getPairs().get(trade.getPair()))
                                .map(pair -> trade.addPairNames(getPairNames(pair))));
    }

    @Override
    public UnaryOperator<Flux<Trade>> addFees(SecurityCredential credential) {
        return tradeFeedIn -> tradeFeedIn
                .doOnNext(trade -> {
                    if (!tradeVolumePairs.contains(trade.getPair())) {
                        tradeVolumePairs.add(trade.getPair());
                    }
                })
                .flatMap(trade -> addFees(getFees(tradeVolume(credential, tradeVolumePairs)), trade));
    }

    private Flux<Map<String, Fees>> getFees(Mono<TradeVolume> tradeVolumeMono) {
        return tradeVolumeMono
                .flux()
                .flatMap(tradeVolume -> Mono.fromCallable(() ->
                                responseHandler
                                        .getFees(tradeVolume))
                        .flatMapIterable(Function.identity())
                        .map(fee -> Map.of(fee.getPair(), fee)));
    }


    private Flux<Trade> addFees(Flux<Map<String, Fees>> feesFlux, Trade trade) {
        return feesFlux
                .map(fees -> trade.setFees(fees.get(
                        trade
                                .getAllNames()
                                .stream()
                                .filter(fees::containsKey)
                                .findFirst()
                                .get())));

    }

    @Override
    public Mono<AssetPairs> assetPairs(String pairs) {
        return client.get()
                .uri(String.format("/0/public/AssetPairs?pair=%1s", pairs))
                .retrieve()
                .bodyToMono(AssetPairs.class)
                .onErrorResume(error -> Mono.error(
                        new ExchangeRequestException(ExchangeName.KRAKEN, error.getMessage())));
    }

    @Override
    public Flux<Balances> balances(SecurityCredential credentials) {
        String endpoint = "/0/private/Balance";
        Map<String, String> bodyValues = Map.of("nonce", nonceCreator.createNonce());

        return getHeaders(client.method(HttpMethod.POST), credentials, bodyValues, endpoint)
                .bodyValue(formatBodyValues(bodyValues))
                .retrieve()
                .bodyToFlux(Balances.class)
                .flatMap(balances -> Mono.fromCallable(() -> balances.checkErrors(balances, balances.getErrors())));
    }

    @Override
    public Mono<TradeVolume> tradeVolume(SecurityCredential credentials, List<String> pairs) {
        String endpoint = String.format("/0/private/TradeVolume?pair=%s", String.join(",", pairs));
        Map<String, String> bodyValues = tradeVolumeRequest(nonceCreator.createNonce(), pairs);

        return getHeaders(client.method(HttpMethod.POST), credentials, bodyValues, endpoint)
                .bodyValue(formatBodyValues(bodyValues))
                .retrieve()
                .bodyToMono(TradeVolume.class)
                .onErrorResume(error -> Mono.error(
                        new ExchangeRequestException(ExchangeName.KRAKEN, error.getMessage())));
    }

    private WebClient.RequestBodySpec getHeaders(WebClient.RequestBodyUriSpec bodySpec,
                                                 SecurityCredential credentials,
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

    private String getSignature(SecurityCredential credentials,
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