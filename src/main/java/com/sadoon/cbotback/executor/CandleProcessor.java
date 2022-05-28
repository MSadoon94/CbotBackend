package com.sadoon.cbotback.executor;

import com.sadoon.cbotback.exchange.model.Candle;
import com.sadoon.cbotback.exchange.model.TickerMessage;
import com.sadoon.cbotback.strategy.StrategyType;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CandleProcessor {

    public Function<Flux<TickerMessage>, Flux<Candle>> toCandles(StrategyType type, Duration bufferDuration){
        return tickerFeed -> tickerFeed
                .buffer(bufferDuration)
                .map(messages -> createCandle(processMessages(messages, type), type));
    }

    private Candle createCandle(Map<String, TickerMessage> messages, StrategyType type){
        return new Candle(
                messages.get("Open").getTimestamp(),
                messages.get("Open").getPrice(type),
                messages.get("High").getPrice(type),
                messages.get("Low").getPrice(type),
                messages.get("Close").getPrice(type)
        );
    }

    private Map<String, TickerMessage> processMessages(List<TickerMessage> messageList, StrategyType type) {
        Map<String, TickerMessage> messages = new HashMap<>(Map.of("Open", messageList.get(0),
                "High", messageList.get(0),
                "Low", messageList.get(0),
                "Close", messageList.get(messageList.size() - 1)));
        return filterMessages(messages, messageList, type);
    }

    private Map<String, TickerMessage> filterMessages(Map<String, TickerMessage> messages,
                                                      List<TickerMessage> messageList,
                                                      StrategyType type) {
        messageList.forEach(message -> {
            String price = message.getPrice(type);
            if (new BigDecimal(price).compareTo(new BigDecimal(messages.get("High").getPrice(type))) > 0) {
                messages.put("High", message);
            }
            if (new BigDecimal(price).compareTo(new BigDecimal(messages.get("Low").getPrice(type))) < 0) {
                messages.put("Low", message);
            }

        });
        return messages;
    }
}
