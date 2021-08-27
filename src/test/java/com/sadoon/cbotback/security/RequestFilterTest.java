package com.sadoon.cbotback.security;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.user.MongoUserDetailsService;
import com.sadoon.cbotback.user.models.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class RequestFilterTest {

    private static final User MOCK_USER =
            new User("username", "password", new SimpleGrantedAuthority("USER"));
    private static final String MOCK_JWT = "Bearer jwt";
    private static final String MOCK_URI = ":3000/api";

    @Autowired
    private AppProperties props;

    @MockBean
    private MongoUserDetailsService mockUserDetailsService;
    @MockBean
    private JwtService mockJwtService;
    @MockBean
    private ExpiredJwtException mockExpiredException;

    private RequestFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        when(mockJwtService.extractUsername(MOCK_JWT.substring(7))).thenReturn(MOCK_USER.getUsername());
        when(mockJwtService.isValidToken(MOCK_JWT.substring(7), MOCK_USER)).thenReturn(true);
        when(mockUserDetailsService.loadUserByUsername(MOCK_USER.getUsername())).thenReturn(MOCK_USER);

        filter = new RequestFilter(props, mockUserDetailsService, mockJwtService);
        setMocks();

    }

    @Test
    void shouldSetSecurityContextForValidatedUsers() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, MOCK_JWT);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), is(MOCK_USER));
    }

    @Test
    void shouldThrowInvalidJwtExceptionForInvalidJwt() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "invalidJwt");

        assertThrows(JwtException.class, () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldThrowExpiredJwtExceptionForNonRefreshTokenRequestsWithExpiredJwt() {
        String jwt = "expiredJwt";
        when(mockJwtService.extractUsername(jwt)).thenReturn(MOCK_USER.getUsername());
        when(mockJwtService.isValidToken(jwt, MOCK_USER)).thenThrow(mockExpiredException);

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        assertThrows(ExpiredJwtException.class, () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldAllowRefreshTokenRequestsForValidJwt() throws ServletException, IOException {
        String jwt = "expiredJwt";

        when(mockJwtService.extractUsername(jwt)).thenThrow(mockExpiredException);

        setRefreshTokenRequest(jwt);
        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), is(MOCK_USER));
    }

    @Test
    void shouldAllowSignupRequests() {
        request.setRequestURI(MOCK_URI + "/signup");
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldAllowLoginRequests() {
        request.setRequestURI(MOCK_URI + "/login");
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
    }

    private void setRefreshTokenRequest(String jwt) {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        request.addHeader("isRefreshToken", true);
        request.setRequestURI(MOCK_URI + "/refreshjwt");
    }

    private void setMocks() {
        mockExpiredException = new ExpiredJwtException
                (
                        null,
                        new DefaultClaims().setSubject(MOCK_USER.getUsername()),
                        null
                );
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }


}
