package com.sadoon.cbotback.security;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.user.MongoUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Arrays;

@Component
public class RequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);

    private String url;

    private final MongoUserDetailsService userDetailsService;

    private final JwtService jwtService;

    public RequestFilter(AppProperties props, MongoUserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.url = props.getCorsExclusion();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException, JwtException {

        if(isPublicEndpoint(request)){
            allowPublicEndpoint();
        } else {
            authenticate(request);
        }

        chain.doFilter(request, response);

    }

    private void authenticate(HttpServletRequest request){
        try {
            String authorization = getJwtCookieValue(request);
            String username  = getUsername(authorization);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                validateToken(userDetails, authorization, request);
            } else {
                logger.error(
                        "User attempted to access non-public endpoint({}) " +
                                "with {} username, authorization value of {} and current authentication of {} .",
                        request.getRequestURI(), username, authorization,
                        SecurityContextHolder.getContext().getAuthentication()
                );
                throw new JwtException("Invalid jwt.");
            }

        } catch (ExpiredJwtException e) {
            logger.info("JWT from User: " + e.getClaims().getSubject() + " is expired.", e);
            handleRequestsWithExpiredJwt(request, e);
        }
    }

    private String getJwtCookieValue(HttpServletRequest request){
        String jwt;

        try {
            jwt =
            Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("jwt"))
                    .findFirst()
                    .orElseThrow(() -> new JwtException("JWT cookie not found."))
                    .getValue();
        } catch (NullPointerException ex){
            throw new JwtException("No cookies found in request.");
        }
        return jwt;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        return request.getRequestURL().toString().equals(url + "/signup")
                || request.getRequestURL().toString().equals(url + "/login");
    }

    private void handleRequestsWithExpiredJwt(HttpServletRequest request, ExpiredJwtException exception) {
        String refreshToken = request.getHeader("isRefreshToken");
        String requestUrl = request.getRequestURL().toString();

        if (refreshToken != null && refreshToken.equals("true")
                && requestUrl.equals(url + "/refresh-jwt")) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(exception.getClaims().getSubject());
            setSecurityContext(userDetails, request);
        } else throw exception;
    }

    private String getUsername(String auth) {
        String username = null;

        if (auth != null) {
            username = jwtService.extractUsername(auth);
        }

        return username;
    }

    private void validateToken(UserDetails userDetails, String auth, HttpServletRequest request) {
        if (jwtService.isValidToken(auth, userDetails)) {
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
