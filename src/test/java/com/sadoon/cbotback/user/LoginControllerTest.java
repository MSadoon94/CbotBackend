package com.sadoon.cbotback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.api.CookieService;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.password.LoginCredentialsException;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private MockMvc mvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LoginService loginService;

    @Mock
    private CookieService cookieService;

    @InjectMocks
    private LoginController loginController;

    private LoginResponse loginResponse = Mocks.loginResponse(new Date());

    @BeforeEach
    public void setUp(){
        mvc = MockMvcBuilders.standaloneSetup(loginController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnLoginResponseOnSuccess() throws Exception {
        given(loginService.handleLogin(any())).willReturn(loginResponse);
        given(cookieService.getRefreshHeaders(any()))
                .willReturn(Mocks.refreshHeaders(Mocks.refreshToken(1000)));

        login().
                andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(loginResponse.getUsername())))
                .andExpect(jsonPath("$.expiration", is(loginResponse.getExpiration().getTime())))
                .andExpect(jsonPath("$.isLoggedIn", is(loginResponse.getIsLoggedIn())));
    }

    @Test
    void shouldReturnUnauthorizedOnLoginFail() throws Exception {
        given(loginService.handleLogin(any())).willReturn(null);

        login().
                andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(new LoginCredentialsException().getMessage())));

    }

    private ResultActions login() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Mocks.loginRequest())));
    }

}