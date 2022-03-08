package com.sadoon.cbotback.api;

import com.sadoon.cbotback.exceptions.notfound.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;

@Service
public class CookieService {

    private RefreshService refreshService;
    private JwtService jwtService;

    public CookieService(RefreshService refreshService, JwtService jwtService) {
        this.refreshService = refreshService;
        this.jwtService = jwtService;
    }

    //Used when refresh token needs to be created.
    public HttpHeaders getRefreshHeaders(Principal principal) throws UserNotFoundException, RefreshTokenNotFoundException {
        RefreshToken token = refreshService.createRefreshToken(principal);
        return getRefreshHeaders(principal, token.getToken());
    }

    public HttpHeaders getRefreshHeaders(Principal principal, String token) throws UserNotFoundException, RefreshTokenNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        RefreshToken refreshToken = refreshService.getRefreshToken(principal, token);
        headers.add(HttpHeaders.SET_COOKIE, getRefreshCookie(refreshToken, "/refresh-jwt").toString());
        headers.add(HttpHeaders.SET_COOKIE, getRefreshCookie(refreshToken, "/log-out").toString());
        return headers;
    }

    public HttpHeaders getJwtHeaders(Principal principal){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, getJwtCookie(jwtService.generateToken(principal.getName())).toString());
        return headers;
    }

    //Used when wanting to add to already created headers.
    public HttpHeaders getJwtHeaders(Principal principal, HttpHeaders headers){
        headers.add(HttpHeaders.SET_COOKIE, getJwtCookie(jwtService.generateToken(principal.getName())).toString());
        return headers;
    }

    private ResponseCookie getRefreshCookie(RefreshToken token, String path) {
        return ResponseCookie
                .from("refresh_token", token.getToken())
                .httpOnly(true)
                .domain("localhost")
                .path(String.format("%s", path))
                .maxAge(Duration.between(Instant.now(), token.getExpiryDate()))
                .build();

    }

    private ResponseCookie getJwtCookie(String jwt){
        return ResponseCookie
                .from("jwt", jwt)
                .httpOnly(true)
                .domain("localhost")
                .path("/")
                .build();
    }
}
