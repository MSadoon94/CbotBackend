package com.sadoon.cbotback.user;

import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.services.TokenService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private AuthenticationManager authenticator;
    private TokenService tokenService;
    private JwtService jwtService;

    public LoginService(AuthenticationManager authenticator, TokenService tokenService, JwtService jwtService) {
        this.authenticator = authenticator;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
    }

    public LoginResponse handleLogin(LoginRequest request) {
        LoginResponse response = null;
        try {
            authenticator.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword())

            );

            RefreshToken refreshToken = tokenService.createRefreshToken(request.getUserId());
            String jwt = jwtService.generateToken(request.getUsername());

            response = new LoginResponse(
                    jwt,
                    jwtService.extractExpiration(jwt));

            response.setHeader(tokenService.getRefreshCookieHeader(refreshToken));

        } catch (BadCredentialsException e) {
            logger.info("Incorrect username or password", e);
        }
        return response;
    }

}
