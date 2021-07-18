package com.sadoon.cbotback.user;

import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.services.TokenService;
import com.sadoon.cbotback.user.models.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class LoginServiceTest {

    private static final LoginRequest LOGIN_REQUEST =
            new LoginRequest("user", "password", "userId");

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken("userId", UUID.randomUUID().toString(), Instant.ofEpochSecond(1));

    private HttpHeaders mockHeader = new HttpHeaders();

    @MockBean
    private AuthenticationManager authenticator;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtService jwtService;

    private LoginService loginService;


    @BeforeEach
    public void setUp() {
        loginService = new LoginService(authenticator, tokenService, jwtService);
    }

    @Test
    void shouldAddRefreshTokenToLoginResponseHeader() {
        setMockHeader();
        when(tokenService.createRefreshToken(LOGIN_REQUEST.getUserId())).thenReturn(REFRESH_TOKEN);
        when(tokenService.getRefreshCookieHeader(REFRESH_TOKEN)).thenReturn(mockHeader);

        HttpHeaders header = loginService.handleLogin(LOGIN_REQUEST).getHeader();

        assertThat(header.get("Set-Cookie").get(0),
                containsString("refresh_token=" + REFRESH_TOKEN.getToken()));
    }

    @Test
    void shouldAddJwtToLoginResponse() {
        when(jwtService.generateToken(LOGIN_REQUEST.getUsername())).thenReturn("jwt");

        assertThat(loginService.handleLogin(LOGIN_REQUEST).getJwt(), is("jwt"));
    }

    private void setMockHeader() {
        mockHeader.add("Set-Cookie",
                "refresh_token=" + REFRESH_TOKEN.getToken() + "; " +
                        "Max-Age=" + REFRESH_TOKEN.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/home; HttpOnly"
        );
    }

}
