package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.auth.RefreshExpiredException;
import com.sadoon.cbotback.exceptions.not_found.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.not_found.UserNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private AppProperties props;
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;

    private RefreshService refreshService;

    private User mockUser = Mocks.user();

    private final Authentication auth = Mocks.auth(mockUser);

    private RefreshToken mockToken;

    @BeforeEach
    public void setUp(){
        refreshService = new RefreshService(props, userService, jwtService);
    }

    @Test
    void shouldDeleteRefreshToken() throws UserNotFoundException {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        refreshService.deleteRefreshToken(auth);

        assertNull(mockUser.getRefreshToken());
    }

    @Test
    void shouldCreateRefreshToken() throws UserNotFoundException {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        assertThat(refreshService.createRefreshToken(auth), is(equalTo(mockUser.getRefreshToken())));
    }

    @Test
    void shouldThrowRefreshExpiredExceptionWhenTokenHasExpired() throws UserNotFoundException {
        setTokenToUser(0);
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        assertThrows(RefreshExpiredException.class, () -> refreshService.refresh(auth, mockToken.getToken()));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTokenIsNotConnectedToUser() throws UserNotFoundException {
        setTokenToUser(10000);

        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        assertThrows(RefreshTokenNotFoundException.class, () -> refreshService.refresh(auth, "invalid"));
    }

    @Test
    void shouldReturnRefreshResponseOnRefreshSuccess() throws UserNotFoundException, RefreshExpiredException, RefreshTokenNotFoundException {
        setTokenToUser(10000);
        Date mockDate = new Date();
        given(jwtService.generateToken(any())).willReturn("mockJwt");
        given(jwtService.extractExpiration("mockJwt")).willReturn(mockDate);
        given(userService.getUserWithUsername(any())).willReturn(mockUser);

        assertThat(refreshService.refresh(auth, mockToken.getToken()),
                samePropertyValuesAs(Mocks.refreshResponse( mockDate)));
    }

    private void setTokenToUser(int tokenExpiration){
        mockToken = Mocks.refreshToken( tokenExpiration );
        mockUser.setRefreshToken(mockToken);
    }

}
