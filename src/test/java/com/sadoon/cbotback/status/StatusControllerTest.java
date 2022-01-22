package com.sadoon.cbotback.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private StatusController controller;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
        mockUser.setCbotStatus(Mocks.cbotStatus());
    }

    @Test
    void shouldReturnOkResponseOnSuccessfulStatusUpdate() throws Exception {
        statusUpdate()
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnCbotStatusOnGetRequest() throws Exception {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        getStatus()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(mockUser.getCbotStatus().isActive())))
                .andExpect(jsonPath("$.activeStrategies", is(mockUser.getCbotStatus().activeStrategies())));
    }

    private ResultActions statusUpdate() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders.put("/user/cbot-status")
                        .principal(auth)
                        .content(objectMapper.writeValueAsString(Mocks.cbotStatus()))
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions getStatus() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders.get("/user/cbot-status")
                        .principal(auth));
    }

}