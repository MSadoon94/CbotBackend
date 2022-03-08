package com.sadoon.cbotback.websocket;

public class BrokerageSocketModule {

    private BrokerageWebSocket webSocketClients;
    private BrokerageMessageFactory messageFactories;

    public BrokerageSocketModule(BrokerageWebSocket webSocketClients,
                                 BrokerageMessageFactory messageFactories) {
        this.webSocketClients = webSocketClients;
        this.messageFactories = messageFactories;
    }

    public BrokerageWebSocket getWebSocketClient(){
        return webSocketClients;
    }

    public BrokerageMessageFactory getMessageFactory(){
        return messageFactories;
    }
}