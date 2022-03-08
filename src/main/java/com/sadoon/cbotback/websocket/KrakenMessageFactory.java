package com.sadoon.cbotback.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.asset.AssetPairs;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

public class KrakenMessageFactory implements BrokerageMessageFactory {

    private final ObjectMapper mapper;

    public KrakenMessageFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<KrakenTickerMessage> tickerMessage(String socketMessage) {
        return Mono.fromCallable(() ->

                        {
                            JsonNode messageNode = mapper.readTree(socketMessage);
                            KrakenTickerMessage message =
                                    mapper.convertValue(messageNode.get(1), KrakenTickerMessage.class);
                            message.setPair(messageNode.get(3).asText());
                            return message;
                        }
                )
                .share()
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> tickerSubscribe(AssetPairs assetPairs) {
        String[] pairs = assetPairs.getPairs().keySet().toArray(new String[]{});
        return Mono.fromCallable(() ->
                        mapper.writeValueAsString(
                                Map.of(
                                        "event", "subscribe",
                                        "pair", pairs,
                                        "subscription", Map.of("name", "ticker")
                                )))
                .subscribeOn(Schedulers.boundedElastic());
    }
}