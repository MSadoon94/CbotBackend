package com.sadoon.cbotback.tools;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

public class WebSocketTest {

    private TestMessageChannel inboundChannel;
    private TestMessageChannel outboundChannel;
    private TestMessageChannel simpMessagingChannel;
    private SimpMessagingTemplate messagingTemplate;
    private TestAnnotationMethodHandler annotationMethodHandler;

    public WebSocketTest(Object handler) {
        inboundChannel = new TestMessageChannel();
        outboundChannel = new TestMessageChannel();
        simpMessagingChannel  = new TestMessageChannel();
        messagingTemplate = new SimpMessagingTemplate(simpMessagingChannel);
        annotationMethodHandler = new TestAnnotationMethodHandler(
                inboundChannel,
                outboundChannel,
                messagingTemplate);
        setAnnotationMethodHandler(handler);

    }

    public TestMessageChannel getInboundChannel() {
        return inboundChannel;
    }

    public TestMessageChannel getOutboundChannel() {
        return outboundChannel;
    }

    public TestMessageChannel getBrokerMessagingChannel() {
        return simpMessagingChannel;
    }

    public SimpMessagingTemplate getMessagingTemplate() {
        return messagingTemplate;
    }

    private void setAnnotationMethodHandler(Object handler){
        annotationMethodHandler.registerHandler(handler);
        annotationMethodHandler.setDestinationPrefixes(List.of("/app", "/topic", "/queue"));
        annotationMethodHandler.setMessageConverter(new MappingJackson2MessageConverter());
        annotationMethodHandler.setApplicationContext(new StaticApplicationContext());
        annotationMethodHandler.afterPropertiesSet();
    }

    public Message<?> responseMessage(StompHeaderAccessor headers){
        Message<byte[]> message =
                MessageBuilder.withPayload(new byte[0]).setHeaders(headers).build();
        annotationMethodHandler.handleMessage(message);
        return outboundChannel.getMessages().get(0);
    }

    public void responseMessage(StompHeaderAccessor headers, byte[] payload){
        Message<byte[]> message =
                MessageBuilder.withPayload(payload).setHeaders(headers).build();
        annotationMethodHandler.handleMessage(message);
    }

    public StompHeaderAccessor subscribeHeaderAccessor(String destination, Principal principal){
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSubscriptionId("0");
        accessor.setDestination(destination);
        accessor.setSessionId("0");
        accessor.setUser(principal);
        accessor.setSessionAttributes(new HashMap<>());
        return accessor;
    }
    public StompHeaderAccessor sendHeaderAccessor(String destination, Principal principal){
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination(destination);
        accessor.setSessionId("0");
        accessor.setUser(principal);
        accessor.setSessionAttributes(new HashMap<>());
        return accessor;
    }

    private static class TestAnnotationMethodHandler extends SimpAnnotationMethodMessageHandler {

        public TestAnnotationMethodHandler(SubscribableChannel inChannel, MessageChannel outChannel,
                                           SimpMessageSendingOperations brokerTemplate) {

            super(inChannel, outChannel, brokerTemplate);
        }

        public void registerHandler(Object handler) {
            super.detectHandlerMethods(handler);
        }
    }
}