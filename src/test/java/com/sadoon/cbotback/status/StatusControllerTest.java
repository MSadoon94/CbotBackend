package com.sadoon.cbotback.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.tools.TestMessageChannel;
import com.sadoon.cbotback.tools.WebSocketTest;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.JsonPathExpectationsHelper;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private StatusController controller;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    @BeforeEach
    void setUp() {
        mockUser.setCbotStatus(Mocks.cbotStatus());
    }

    @Test
    void shouldReturnCbotStatusOnSubscribe() throws Exception {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        WebSocketTest webSocketTest = new WebSocketTest(controller, new SimpMessagingTemplate(new TestMessageChannel()));

        webSocketTest.sendMessageToController(webSocketTest.subscribeHeaderAccessor("/topic/cbot-status", auth));

        Message<?> reply = webSocketTest.getOutboundChannel().getMessages().get(0);

        String responseJson = new String((byte[]) reply.getPayload(), StandardCharsets.UTF_8);

        new JsonPathExpectationsHelper("$.isActive")
                .assertValue(responseJson, is(mockUser.getCbotStatus().isActive()));
        new JsonPathExpectationsHelper("$.activeStrategies")
                .assertValue(responseJson, is(mockUser.getCbotStatus().activeStrategies()));
    }

    @Test
    void shouldSendStatusUpdatesToBrokerOnIncomingMessage() throws JsonProcessingException, UserNotFoundException {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        given(userService.updateStatus(any(), any())).willReturn(mockUser);
        WebSocketTest webSocketTest = new WebSocketTest(controller, new SimpMessagingTemplate(new TestMessageChannel()));

        webSocketTest.sendMessageToController(
                webSocketTest.sendHeaderAccessor("/app/cbot-status", auth),
                Mocks.mapper.writeValueAsBytes(Mocks.cbotStatus()));

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);

        assertThat(reply.getPayload(), is(mockUser.getCbotStatus()));
    }
}