package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.exceptions.RefreshException;
import com.sadoon.cbotback.exceptions.RefreshExpiredException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshService {

    private final JwtService jwtService;
    private final Long refreshTokenDurationMs;
    private final RefreshTokenRepository repo;

    public RefreshService(AppProperties props, RefreshTokenRepository repo, JwtService jwtService) {
        this.refreshTokenDurationMs = props.getRefreshTokenDurationMs();
        this.repo = repo;
        this.jwtService = jwtService;
    }

    public RefreshResponse refresh(Principal principal, String refreshToken) throws RefreshTokenNotFoundException, RefreshExpiredException {

        return getResponse(
                principal.getName(),
                verifyExpiration(getRefreshToken(refreshToken)));
    }

    public RefreshToken createRefreshToken(String userId) {
        return repo.save(
                new RefreshToken(
                        userId,
                        UUID.randomUUID().toString(),
                        Instant.now().plusMillis(refreshTokenDurationMs)
                ));
    }

    public void deleteRefreshToken(RefreshToken refreshToken) {
        repo.delete(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) throws RefreshExpiredException {

        if (isExpired(token)) {
            repo.delete(token);
            throw new RefreshExpiredException();
        }
        return token;
    }

    public RefreshToken getRefreshToken(String token) throws RefreshTokenNotFoundException {
        return repo.findByToken(token)
                .orElseThrow(() -> new RefreshTokenNotFoundException(token));
    }

    private RefreshResponse getResponse(String username, RefreshToken refreshToken) throws RefreshException {
        String jwt = jwtService.generateToken(username);

        if (isExpired(refreshToken)) {
            repo.delete(refreshToken);
            throw new RefreshException(refreshToken.getToken(),
                    "Refresh token was expired. Please make a new sign in request");
        }

        RefreshResponse response =
                new RefreshResponse(jwt, jwtService.extractExpiration(jwt));
        response.setHeaders(getRefreshCookieHeader(refreshToken));

        return response;
    }

    public ResponseCookie getResponseCookie(RefreshToken refreshToken, String path){
       return ResponseCookie
               .from("refresh_token", refreshToken.getToken())
               .httpOnly(true)
               .domain("localhost")
               .path(String.format("/api%s", path))
               .maxAge(Duration.between(Instant.now(), refreshToken.getExpiryDate()))
               .build();

    }

    public HttpHeaders getRefreshCookieHeader(RefreshToken refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", getRefreshCookieHeaderValue(refreshToken, "/refresh-jwt"));
        headers.add("Set-Cookie", getRefreshCookieHeaderValue(refreshToken, "/log-out"));
        return headers;
    }

    private boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) <= 0;
    }

    private String getRefreshCookieHeaderValue(RefreshToken refreshToken, String path) {
        return "refresh_token=" + refreshToken.getToken() + "; " +
                "Max-Age=" + refreshToken.getExpiryDate() + "; " +
                "Domain=localhost; Path=/api" + path + "; HttpOnly";
    }


}
