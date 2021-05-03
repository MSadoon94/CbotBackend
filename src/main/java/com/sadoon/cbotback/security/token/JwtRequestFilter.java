package com.sadoon.cbotback.security.token;

import com.sadoon.cbotback.security.services.MongoUserDetailsService;
import com.sadoon.cbotback.security.token.services.JwtService;
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
public class JwtRequestFilter extends OncePerRequestFilter {

    private MongoUserDetailsService userDetailsService;

    private JwtService jwtService;

    public JwtRequestFilter(MongoUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        String username = getUsername(authorizationHeader);

        String isRefreshToken = request.getHeader("isRefreshToken");
        String requestURL = request.getRequestURL().toString();

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            validateToken(userDetails, authorizationHeader, request);
        } else if (isRefreshToken != null && isRefreshToken.equals("true") && requestURL.contains("refreshjwt")) {
            allowRefreshToken();
        }
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

    private void validateToken(UserDetails userDetails, String header, HttpServletRequest request) {
        if (jwtService.isValidToken(getJwt(header), userDetails)) {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(token);
        }
    }

    private void allowRefreshToken() {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(null, null, null);

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

    }
}
