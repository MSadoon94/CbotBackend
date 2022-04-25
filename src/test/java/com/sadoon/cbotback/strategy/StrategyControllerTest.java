package com.sadoon.cbotback.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
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
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StrategyControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    private SimpMessagingTemplate messagingTemplate = new SimpMessagingTemplate(new TestMessageChannel());

    @InjectMocks
    private StrategyController controller;

    private MockMvc mvc;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    private Strategy mockStrategy = Mocks.strategy();

    private WebSocketTest webSocketTest;

    @BeforeEach
    void setUp() {
        controller = new StrategyController(userService, messagingTemplate);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
        webSocketTest = new WebSocketTest(controller, messagingTemplate);
    }

    @Test
    void shouldReturnStrategyNamesOnSubscribe() throws UserNotFoundException {
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        webSocketTest.sendMessageToController(
                webSocketTest.subscribeHeaderAccessor("/topic/strategies/details", auth)
        );

        Message<?> reply = webSocketTest.getOutboundChannel().getMessages().get(0);

        String responseJson = new String((byte[]) reply.getPayload(), StandardCharsets.UTF_8);

        new JsonPathExpectationsHelper("$")
                .assertValueIsArray(responseJson);
        assertThat(responseJson, stringContainsInOrder(mockStrategy.getName()));

    }

    @Test
    void shouldSendActiveStrategyUpdatesToBrokerOnIncomingMessage() throws UserNotFoundException, JsonProcessingException {
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        webSocketTest.sendMessageToController(
                webSocketTest.sendHeaderAccessor(
                        String.format("/app/strategies/%1s/%2s", mockStrategy.getName(), true),
                        auth
                ),
                Mocks.mapper.writeValueAsBytes(mockStrategy.getName()));

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);
        List<Map<String, String>> details = (List<Map<String, String>>) reply.getPayload();

        assertThat(details.get(0).get("name"), is(mockStrategy.getName()));
        assertThat(details.get(0).get("isActive"), is("true"));
    }

    @Test
    void shouldReturnCreatedStatusOnSaveStrategySuccess() throws Exception {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        saveStrategy()
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnAllStrategiesOnLoadStrategiesSuccess() throws Exception {
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        loadStrategies()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey(mockStrategy.getName())));
    }

    @Test
    void shouldReturnStrategyOnLoadStrategySuccess() throws Exception {
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        loadStrategy()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(mockStrategy.getName())));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFound() throws Exception {
        given(userService.getUserWithUsername(any()))
                .willThrow(new UserNotFoundException(mockUser.getUsername()));
        saveStrategy().andExpect(status().isNotFound());
        loadStrategies().andExpect(status().isNotFound());
        loadStrategy().andExpect(status().isNotFound());
    }

    @Test
    void shouldSendStrategyDetailUpdateOnStrategyAddition() throws Exception {
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        saveStrategy()
                .andExpect(status().isCreated());

        Message<?> reply = webSocketTest.getBrokerMessagingChannel().getMessages().get(0);
        List<Map<String, String>> details = (List<Map<String, String>>) reply.getPayload();

        assertThat(details.get(0).get("name"), is(mockStrategy.getName()));
        assertThat(details.get(0).get("isActive"), is("false"));
    }

    private ResultActions saveStrategy() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders
                        .post("/user/strategy")
                        .content(objectMapper.writeValueAsString(mockStrategy))
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(auth)
        );
    }

    private ResultActions loadStrategies() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders
                        .get("/user/strategies")
                        .principal(auth)
        );
    }

    private ResultActions loadStrategy() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/user/strategy/%s", mockStrategy.getName()))
                        .principal(auth)
        );
    }

}
