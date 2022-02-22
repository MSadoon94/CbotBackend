package com.sadoon.cbotback.security;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.user.MongoUserDetailsService;
import com.sadoon.cbotback.user.models.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestFilterTest {

    private static final String MOCK_URI = ":3000/api";

    @Mock
    private AppProperties props;

    @Mock
    private MongoUserDetailsService mockUserDetailsService;
    @Mock
    private JwtService mockJwtService;
    @Mock
    private ExpiredJwtException mockExpiredException;

    private RequestFilter filter;

    private User mockUser = Mocks.user();

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        when(props.getCorsExclusion()).thenReturn("http://localhost:3000/api");

        filter = new RequestFilter(props, mockUserDetailsService, mockJwtService);
        setMocks();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetSecurityContextForValidatedUsers() throws ServletException, IOException {
        request.setCookies(new Cookie("jwt", "mockJwt"));

        when(mockJwtService.extractUsername(any())).thenReturn(mockUser.getUsername());
        when(mockUserDetailsService.loadUserByUsername(any())).thenReturn(mockUser);
        when(mockJwtService.isValidToken(any(), any())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), is(mockUser));
    }

    @Test
    void shouldThrowInvalidJwtExceptionForUnauthenticatedJwt() {
        request.setCookies(new Cookie("jwt", "mockJwt"));

        when(mockJwtService.extractUsername(any())).thenReturn(mockUser.getUsername());
        when(mockUserDetailsService.loadUserByUsername(any())).thenReturn(mockUser);
        when(mockJwtService.isValidToken(any(), any())).thenReturn(false);

        Exception exception = assertThrows(
                JwtException.class,
                () -> filter.doFilterInternal(request, response, filterChain)
        );

        assertThat(exception.getMessage(), is("Jwt cannot be authenticated."));
    }

    @Test
    void shouldThrowJwtExceptionForNullJwtValue() {
        request.setCookies(new Cookie("jwt", null));
        Exception exception = assertThrows(
                JwtException.class,
                () -> filter.doFilterInternal(request, response, filterChain)
        );


        assertThat(exception.getMessage(), is("Invalid jwt."));
    }

    @Test
    void shouldThrowJwtExceptionIfNoCookiesInRequest() {
        Exception exception = assertThrows(
                JwtException.class,
                () -> filter.doFilterInternal(request, response, filterChain)
        );

        assertThat(exception.getMessage(), is("No cookies found in request."));
    }

    @Test
    void shouldThrowJwtExceptionIfNoJwtCookie() {
        request.setCookies(new Cookie("notJwt", "notJwt"));
        Exception exception = assertThrows(
                JwtException.class,
                () -> filter.doFilterInternal(request, response, filterChain)
        );

        assertThat(exception.getMessage(), is("JWT cookie not found."));
    }

    @Test
    void shouldThrowExpiredJwtExceptionForNonRefreshTokenRequestsWithExpiredJwt() {
        request.setCookies(new Cookie("jwt", "expiredJwt"));
        when(mockJwtService.extractUsername(any())).thenReturn(mockUser.getUsername());
        when(mockJwtService.isValidToken(any(), any())).thenThrow(mockExpiredException);

        assertThrows(ExpiredJwtException.class, () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldAllowRefreshTokenRequestsForValidJwt() throws ServletException, IOException {
        when(mockJwtService.extractUsername(any())).thenThrow(mockExpiredException);
        when(mockUserDetailsService.loadUserByUsername(any())).thenReturn(mockUser);
        setRefreshTokenRequest();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), is(mockUser));
    }

    @Test
    void shouldAllowSignupRequests() {
        request.setCookies(new Cookie("jwt", "mockJwt"));
        request.setRequestURI(MOCK_URI + "/sign-up");

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldAllowLoginRequests() {
        request.setCookies(new Cookie("jwt", "mockJwt"));
        request.setRequestURI(MOCK_URI + "/login");

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

    private void setRefreshTokenRequest() {
        request.addHeader("isRefreshToken", true);
        request.setRequestURI(MOCK_URI + "/refresh-jwt");
        request.setCookies(
                Mocks.refreshCookie("/refresh-jwt", 10000),
                new Cookie("jwt", "mockJwt")
        );
    }

    private void setMocks() {
        mockExpiredException = new ExpiredJwtException
                (
                        null,
                        new DefaultClaims().setSubject(mockUser.getUsername()),
                        null
                );
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }
}
