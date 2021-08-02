package com.sadoon.cbotback.security;

import com.sadoon.cbotback.exceptions.ExpiredJwtException;
import com.sadoon.cbotback.exceptions.InvalidJwtException;
import com.sadoon.cbotback.user.MongoUserDetailsService;
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

    private MongoUserDetailsService userDetailsService;

    private JwtService jwtService;

    public RequestFilter(MongoUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException, InvalidJwtException {

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        String username = getUsername(authorizationHeader);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            validateToken(userDetails, authorizationHeader, request);
        } else throw new InvalidJwtException(authorizationHeader, "Invalid jwt or authorization header.");


        chain.doFilter(request, response);

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

    private void validateToken(UserDetails userDetails, String header, HttpServletRequest request) throws InvalidJwtException {
        String refreshToken = request.getHeader("isRefreshToken");
        String requestUrl = request.getRequestURL().toString();
        try {

            if (jwtService.isValidToken(getJwt(header), userDetails)) {
                setSecurityContext(userDetails, request);
            } else throw new InvalidJwtException(getJwt(header), "Jwt cannot be authenticated.");

        } catch (ExpiredJwtException exception) {
            if (refreshToken != null && refreshToken.equals("true")
                    && requestUrl.contains("refreshjwt")) {
                allowRefreshToken();
            } else throw exception;
        }
    }

    private void setSecurityContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void allowRefreshToken() {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(null, null, null);

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

    }
}
