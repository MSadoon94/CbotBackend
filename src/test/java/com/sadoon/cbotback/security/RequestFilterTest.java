package com.sadoon.cbotback.security;

import com.sadoon.cbotback.exceptions.ExpiredJwtException;
import com.sadoon.cbotback.exceptions.InvalidJwtException;
import com.sadoon.cbotback.user.MongoUserDetailsService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class RequestFilterTest {

    private static final User MOCK_USER =
            new User("username", "password", new SimpleGrantedAuthority("USER"));
    private static final String MOCK_JWT = "Bearer jwt";

    @MockBean
    private MongoUserDetailsService mockUserDetailsService;
    @MockBean
    private JwtService mockJwtService;
    @MockBean
    private RequestFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        when(mockJwtService.extractUsername(MOCK_JWT.substring(7))).thenReturn(MOCK_USER.getUsername());
        when(mockJwtService.isValidToken(MOCK_JWT.substring(7), MOCK_USER)).thenReturn(true);
        when(mockUserDetailsService.loadUserByUsername(MOCK_USER.getUsername())).thenReturn(MOCK_USER);

        filter = new RequestFilter(mockUserDetailsService, mockJwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void shouldSetSecurityContextForValidatedUsers() {
        request.addHeader(HttpHeaders.AUTHORIZATION, MOCK_JWT);

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), is(MOCK_USER));
    }

    @Test
    void shouldThrowInvalidJwtExceptionForInvalidJwt() {
        request.addHeader(HttpHeaders.AUTHORIZATION, "invalidJwt");

        assertThrows(InvalidJwtException.class, () -> filter.doFilterInternal(request, response, filterChain));
    }

    @Test
    void shouldThrowExpiredJwtExceptionForNonRefreshTokenRequestsWithExpiredJwt() {
        String jwt = "expiredJwt";
        when(mockJwtService.extractUsername(jwt)).thenReturn(MOCK_USER.getUsername());
        when(mockJwtService.isValidToken(jwt, MOCK_USER)).thenThrow(new ExpiredJwtException(jwt, "Jwt is expired."));

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        assertThrows(ExpiredJwtException.class, () -> filter.doFilter(request, response, filterChain));
    }

    @Test
    void shouldAllowRefreshTokenRequestsForValidJwt() {
        String jwt = "expiredJwt";
        when(mockJwtService.extractUsername(jwt)).thenReturn(MOCK_USER.getUsername());
        when(mockJwtService.isValidToken(jwt, MOCK_USER)).thenThrow(new ExpiredJwtException(jwt, "Jwt is expired."));

        setRefreshTokenRequest(jwt);

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));
    }

    private void setRefreshTokenRequest(String jwt) {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        request.addHeader("isRefreshToken", true);
        request.setRequestURI("/refreshjwt");
    }


}
