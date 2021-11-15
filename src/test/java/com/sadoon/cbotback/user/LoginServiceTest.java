package com.sadoon.cbotback.user;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.LoginCredentialsException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticator;

    @Mock
    private RefreshService refreshService;

    @Mock
    private JwtService jwtService;

    private LoginService loginService;

    private LoginRequest loginRequest = Mocks.loginRequest();

    private User mockUser = Mocks.user();

    private Authentication auth = Mocks.auth(mockUser);

    private RefreshToken mockToken;

    private HttpHeaders mockHeaders = new HttpHeaders();

    @BeforeEach
    public void setUp() {
        loginService = new LoginService(authenticator, refreshService, jwtService);
    }

    @Test
    void shouldAddRefreshTokenToLoginResponseHeader() throws UserNotFoundException, RefreshTokenNotFoundException {
        setTokenToUser(10000);
        ResponseCookie refreshJwtCookie = Mocks.responseCookie(mockToken, "/refresh-jwt");
        ResponseCookie logoutCookie = Mocks.responseCookie(mockToken, "/log-out");
        setMockHeaders(refreshJwtCookie, logoutCookie);

        when(refreshService.createRefreshToken(any())).thenReturn(mockToken);
        when(refreshService.getResponseCookie(auth, mockToken.getToken(), "/refresh-jwt"))
                .thenReturn(refreshJwtCookie);
        when(refreshService.getResponseCookie(auth, mockToken.getToken(), "/log-out"))
                .thenReturn(logoutCookie);

        HttpHeaders headers = loginService.getHeaders(auth);

        assertThat(headers, is(mockHeaders));
    }

    @Test
    void shouldAddJwtToLoginResponse() {
        when(jwtService.generateToken(loginRequest.getUsername())).thenReturn("jwt");

        assertThat(loginService.handleLogin(loginRequest).getJwt(), is("jwt"));
    }

    @Test
    void shouldAddUsernameToLoginResponse() {
        assertThat(loginService.handleLogin(loginRequest).getUsername(), is(loginRequest.getUsername()));
    }

    @Test
    void shouldReturnAuthenticatedUserAsPrincipal() throws LoginCredentialsException {
        when(authenticator.authenticate(any())).thenReturn(auth);
        assertThat(loginService.getPrincipal(loginRequest).getName(), is(loginRequest.getUsername()));
    }

    @Test
    void shouldThrowLoginCredentialsExceptionOnAuthenticateFail(){
        when(authenticator.authenticate(any())).thenThrow(new BadCredentialsException("BadCredentials"));

        assertThrows(LoginCredentialsException.class, () -> loginService.getPrincipal(loginRequest));
    }

    private void setTokenToUser(int tokenExpiration){
        mockToken = Mocks.refreshToken( tokenExpiration );
        mockUser.setRefreshToken(mockToken);
    }

    private void setMockHeaders(ResponseCookie... cookies){
        for(ResponseCookie cookie : cookies){
            mockHeaders.add("Set-Cookie", cookie.toString());
        }
    }

}
