package com.sadoon.cbotback.exchange.meta;

import org.springframework.web.reactive.socket.WebSocketMessage;

import java.util.Arrays;
import java.util.stream.Stream;

public enum PayloadType {
    EVENT,
    UNKNOWN,
    TICKER;

    public static Stream<PayloadType> getStream() {
        return Arrays.stream(PayloadType.values());
    }

    public static PayloadType getType(WebSocketMessage message){
        return PayloadType.getStream()
                .filter(type -> message.getPayloadAsText().contains(type.name().toLowerCase()))
                .findFirst().orElse(PayloadType.UNKNOWN);
    }
    public static PayloadType getType(String message){
        return PayloadType.getStream()
                .filter(type -> message.contains(type.name().toLowerCase()))
                .findFirst().orElse(PayloadType.UNKNOWN);
    }
}