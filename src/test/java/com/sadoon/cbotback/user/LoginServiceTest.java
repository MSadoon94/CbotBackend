package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.models.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SpringBootTest
class LoginServiceTest {

    private static final LoginRequest LOGIN_REQUEST =
            new LoginRequest("user", "password", "userId");

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken(UUID.randomUUID().toString(), Instant.ofEpochSecond(1));

    private final HttpHeaders mockHeader = new HttpHeaders();

    @MockBean
    private AuthenticationManager authenticator;

    @MockBean
    private RefreshService refreshService;

    @MockBean
    private JwtService jwtService;

    private LoginService loginService;


    @BeforeEach
    public void setUp() {
        loginService = new LoginService(authenticator, refreshService, jwtService);
    }

    @Test
    void shouldAddRefreshTokenToLoginResponseHeader() throws UserNotFoundException {
        setMockHeader();
        when(refreshService.createRefreshToken(LOGIN_REQUEST.getUserId())).thenReturn(REFRESH_TOKEN);
        when(refreshService.getRefreshCookieHeader(REFRESH_TOKEN)).thenReturn(mockHeader);

        HttpHeaders header = loginService.getHeader(LOGIN_REQUEST.getUserId());

        assertThat(header.get("Set-Cookie").get(0),
                containsString("refresh_token=" + REFRESH_TOKEN.getToken()));
    }

    @Test
    void shouldAddJwtToLoginResponse() {
        when(jwtService.generateToken(LOGIN_REQUEST.getUsername())).thenReturn("jwt");

        assertThat(loginService.handleLogin(LOGIN_REQUEST).getJwt(), is("jwt"));
    }

    @Test
    void shouldAddUsernameToLoginResponse() {
        assertThat(loginService.handleLogin(LOGIN_REQUEST).getUsername(), is(LOGIN_REQUEST.getUsername()));
    }

    private void setMockHeader() {
        mockHeader.add("Set-Cookie",
                "refresh_token=" + REFRESH_TOKEN.getToken() + "; " +
                        "Max-Age=" + REFRESH_TOKEN.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/home; HttpOnly"
        );
    }

}
