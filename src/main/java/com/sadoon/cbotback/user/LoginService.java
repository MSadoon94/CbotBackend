package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.LoginCredentialsException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class LoginService {

    private AuthenticationManager authenticator;
    private RefreshService refreshService;
    private JwtService jwtService;

    public LoginService(AuthenticationManager authenticator, RefreshService refreshService, JwtService jwtService) {
        this.authenticator = authenticator;
        this.refreshService = refreshService;
        this.jwtService = jwtService;
    }

    public Principal getPrincipal(LoginRequest request) throws LoginCredentialsException {
        Principal principal;
        try {
         principal = authenticator.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword())

        );
        } catch (BadCredentialsException e) {
            throw new LoginCredentialsException(new ApiError(HttpStatus.UNAUTHORIZED, e.getMessage(), e));
        }
         return principal;
    }

    public LoginResponse handleLogin(LoginRequest request) {
        LoginResponse response;

            String jwt = jwtService.generateToken(request.getUsername());

            response = new LoginResponse(
                    request.getUsername(),
                    jwt,
                    jwtService.extractExpiration(jwt));

        return response;
    }

    public HttpHeaders getHeaders(Principal principal) throws UserNotFoundException, RefreshTokenNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        RefreshToken token = refreshService.createRefreshToken(principal);
        headers.add(HttpHeaders.SET_COOKIE,
                refreshService.getResponseCookie(principal, token.getToken(), "/refresh-jwt").toString());
        headers.add(HttpHeaders.SET_COOKIE,
                refreshService.getResponseCookie(principal, token.getToken(), "/log-out").toString());
        return headers;
    }

}
