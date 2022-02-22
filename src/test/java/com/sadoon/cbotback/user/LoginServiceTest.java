package com.sadoon.cbotback.user;

import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.password.LoginCredentialsException;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private JwtService jwtService;

    private LoginService loginService;

    private LoginRequest loginRequest = Mocks.loginRequest();

    private User mockUser = Mocks.user();

    private Authentication auth = Mocks.auth(mockUser);

    @BeforeEach
    public void setUp() {
        loginService = new LoginService(authenticator, jwtService);
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

}
