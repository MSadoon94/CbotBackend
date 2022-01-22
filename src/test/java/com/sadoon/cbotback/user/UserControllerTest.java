package com.sadoon.cbotback.user;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class UserControllerTest {

    private MockMvc mvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    @BeforeEach
    void setUp(){
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    void shouldReturnUserOnSuccess() throws Exception {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        getUser()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mockUser.getId())))
                .andExpect(jsonPath("$.username", is(mockUser.getUsername())))

                .andExpect(jsonPath("$.authorities[0].authority",
                        is(mockUser.getAuthorities().get(0).getAuthority())))

                .andExpect(jsonPath("$.refreshToken", is(mockUser.getRefreshToken())))
                .andExpect(jsonPath("$.cards", is(mockUser.getCards())))
                .andExpect(jsonPath("$.strategies", is(mockUser.getStrategies())))
                .andExpect(jsonPath("$.cbotStatus.isActive", is(mockUser.getCbotStatus().isActive())))
                .andExpect(jsonPath("$.cbotStatus.activeStrategies", is(mockUser.getCbotStatus().activeStrategies())));
    }

    private ResultActions getUser() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders.get("/user")
                        .principal(auth)
        );
    }

}