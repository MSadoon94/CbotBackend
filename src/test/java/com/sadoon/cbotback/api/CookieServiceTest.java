package com.sadoon.cbotback.api;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CookieServiceTest {

    @Mock
    private RefreshService refreshService;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;

    private CookieService cookieService;

    private User mockUser = Mocks.user();

    private final Authentication auth = Mocks.auth(mockUser);

    private RefreshToken mockToken;

    private HttpHeaders mockHeaders = new HttpHeaders();

    @BeforeEach
    public void setUp(){
        cookieService = new CookieService(refreshService);
    }

    @Test
    void shouldReturnRefreshCookieHeaders() throws UserNotFoundException, RefreshTokenNotFoundException {
        setTokenToUser(10000);
        given(refreshService.createRefreshToken(any())).willReturn(mockToken);
        given(refreshService.getRefreshToken(any(), any())).willReturn(mockToken);

        ResponseCookie refreshJwtCookie = Mocks.responseCookie(mockToken, "/refresh-jwt");
        ResponseCookie logoutCookie = Mocks.responseCookie(mockToken, "/log-out");
        setMockHeaders(refreshJwtCookie, logoutCookie);


        assertThat(cookieService.getRefreshHeaders(auth), is(mockHeaders));
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
