package com.sadoon.cbotback.security;

import com.sadoon.cbotback.user.MongoUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);

    private static final String URL = "http://localhost:8080";

    private final MongoUserDetailsService userDetailsService;

    private final JwtService jwtService;

    public RequestFilter(MongoUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException, JwtException {

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            String username = getUsername(authorizationHeader);
            if (isPublicEndpoint(request)) {
                allowPublicEndpoint();
            } else if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                validateToken(userDetails, authorizationHeader, request);
            } else throw new JwtException("Invalid jwt or authorization header.");

        } catch (ExpiredJwtException e) {
            logger.info("JWT from User: " + e.getClaims().getSubject() + " is expired.", e);
            handleRequestsWithExpiredJwt(request, e);
        }

        chain.doFilter(request, response);

    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        return request.getRequestURL().toString().equals(URL + "/signup")
                || request.getRequestURL().toString().equals(URL + "/login");
    }

    private void handleRequestsWithExpiredJwt(HttpServletRequest request, ExpiredJwtException exception) {
        String refreshToken = request.getHeader("isRefreshToken");
        String requestUrl = request.getRequestURL().toString();

        if (refreshToken != null && refreshToken.equals("true")
                && requestUrl.equals(URL + "/refreshjwt")) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(exception.getClaims().getSubject());
            setSecurityContext(userDetails, request);
        } else throw exception;
    }

    private String getUsername(String header) {
        String username = null;

        if (header != null && header.startsWith("Bearer ")) {
            username = jwtService.extractUsername(getJwt(header));
        }

        return username;
    }

    private String getJwt(String header) {
        return header.substring(7);
    }

    private void validateToken(UserDetails userDetails, String header, HttpServletRequest request) {
        if (jwtService.isValidToken(getJwt(header), userDetails)) {
            setSecurityContext(userDetails, request);
        } else throw new JwtException("Jwt cannot be authenticated.");
    }


    private void setSecurityContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void allowPublicEndpoint() {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(null, null, null);

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

    }
}
