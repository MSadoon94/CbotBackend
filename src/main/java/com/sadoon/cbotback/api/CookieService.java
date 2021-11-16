package com.sadoon.cbotback.api;

import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;

@Service
public class CookieService {

    private RefreshService refreshService;

    public CookieService(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    //Used when refresh token needs to be created.
    public HttpHeaders getRefreshHeaders(Principal principal) throws UserNotFoundException, RefreshTokenNotFoundException {
        RefreshToken token = refreshService.createRefreshToken(principal);
        return getRefreshHeaders(principal, token.getToken());
    }

    public HttpHeaders getRefreshHeaders(Principal principal, String token) throws UserNotFoundException, RefreshTokenNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, getRefreshCookie(principal, token, "/refresh-jwt").toString());
        headers.add(HttpHeaders.SET_COOKIE, getRefreshCookie(principal, token, "/log-out").toString());
        return headers;
    }
    private ResponseCookie getRefreshCookie(Principal principal, String token, String path) throws UserNotFoundException, RefreshTokenNotFoundException {
        RefreshToken refreshToken = refreshService.getRefreshToken(principal, token);

        return ResponseCookie
                .from("refresh_token", refreshToken.getToken())
                .httpOnly(true)
                .domain("localhost")
                .path(String.format("/api%s", path))
                .maxAge(Duration.between(Instant.now(), refreshToken.getExpiryDate()))
                .build();

    }
}
