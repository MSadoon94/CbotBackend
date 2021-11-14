package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private AuthenticationManager authenticator;
    private RefreshService refreshService;
    private JwtService jwtService;

    public LoginService(AuthenticationManager authenticator, RefreshService refreshService, JwtService jwtService) {
        this.authenticator = authenticator;
        this.refreshService = refreshService;
        this.jwtService = jwtService;
    }

    public LoginResponse handleLogin(LoginRequest request) {
        LoginResponse response = null;
        try {
            authenticator.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword())

            );

            String jwt = jwtService.generateToken(request.getUsername());

            response = new LoginResponse(
                    request.getUsername(),
                    jwt,
                    jwtService.extractExpiration(jwt));

        } catch (BadCredentialsException e) {
            logger.info("Incorrect username or password", e);
        }
        return response;
    }

    public HttpHeaders getHeader(String userId) throws UserNotFoundException {

        return refreshService.getRefreshCookieHeader(refreshService.createRefreshToken(userId));
    }

}
