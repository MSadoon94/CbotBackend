package com.sadoon.cbotback.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.net.URI;

@Component
public class BrokerageSocketComponents {

    @Bean
    @Lazy
    ObjectMapper mapper(){
        return new ObjectMapper();
    }

    @Bean(name = "kraken")
    @Lazy
    BrokerageSocketModule krakenBrokerageSocketService(ObjectMapper mapper){
        return new BrokerageSocketModule(
                new BrokerageWebSocket(new ReactorNettyWebSocketClient(), URI.create("wss://ws.kraken.com")),
                new KrakenMessageFactory(mapper)
        );
    }
}