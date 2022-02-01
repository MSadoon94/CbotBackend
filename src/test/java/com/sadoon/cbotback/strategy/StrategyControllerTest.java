package com.sadoon.cbotback.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StrategyControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService mockUserService;

    @InjectMocks
    private StrategyController controller;

    private MockMvc mvc;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    private Strategy mockStrategy = Mocks.strategy();

    @BeforeEach
    void setUp(){
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    void shouldReturnCreatedStatusOnSaveStrategySuccess() throws Exception {
        saveStrategy()
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnAllStrategiesOnLoadStrategiesSuccess() throws Exception {
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(mockUserService.getUserWithUsername(any())).willReturn(mockUser);

        loadStrategies()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey(mockStrategy.getName())));
    }

    @Test
    void shouldReturnStrategyOnLoadStrategySuccess() throws Exception{
        mockUser.setStrategies(Map.of(mockStrategy.getName(), mockStrategy));
        given(mockUserService.getUserWithUsername(any())).willReturn(mockUser);

        loadStrategy()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(mockStrategy.getName())));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFound() throws Exception {
        given(mockUserService.getUserWithUsername(any()))
                .willThrow(new UserNotFoundException(mockUser.getUsername()));
        saveStrategy().andExpect(status().isNotFound());
        loadStrategies().andExpect(status().isNotFound());
        loadStrategy().andExpect(status().isNotFound());
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
