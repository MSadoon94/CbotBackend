package com.sadoon.cbotback.user;
import com.sadoon.cbotback.security.token.services.TokenService;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.user.models.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class LoginServiceTest {

    private static final LoginRequest LOGIN_REQUEST =
            new LoginRequest("user", "password", "userId");

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken("userId", Instant.ofEpochSecond(1));


    @MockBean
    private AuthenticationManager authenticator;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtService jwtService;

    private LoginService loginService;



    @BeforeEach
    public void setUp(){
        loginService = new LoginService(authenticator, tokenService, jwtService);
    }

    @Test
    void shouldAddRefreshTokenToLoginResponse(){
        when(tokenService.createRefreshToken(LOGIN_REQUEST.getUserId())).thenReturn(REFRESH_TOKEN);

        assertThat(loginService.handleLogin(LOGIN_REQUEST).getRefreshToken(), samePropertyValuesAs(REFRESH_TOKEN));
    }

    @Test
    void shouldAddJwtToLoginResponse(){
        when(jwtService.generateToken(LOGIN_REQUEST.getUsername())).thenReturn("jwt");

        assertThat(loginService.handleLogin(LOGIN_REQUEST).getJwt(), is("jwt"));
    }

}
