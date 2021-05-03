package com.sadoon.cbotback.security.token.services;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.TokenRefreshException;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshService {

    private JwtService jwtService;
    private Long refreshTokenDurationMs;
    private RefreshTokenRepository repo;

    public RefreshService(AppProperties props, RefreshTokenRepository repo, JwtService jwtService) {
        this.refreshTokenDurationMs = props.getRefreshTokenDurationMs();
        this.repo = repo;
        this.jwtService = jwtService;
    }

    public TokenResponse refresh(TokenRequest request, String refreshToken) {

        return getResponse(
                request.getUsername(),
                getRefreshToken(refreshToken));
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

    public RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException {

        if (isExpired(token)) {
            repo.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public RefreshToken getRefreshToken(String token) throws TokenRefreshException {
        return repo.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token is not in database!"));
    }

    private TokenResponse getResponse(String username, RefreshToken refreshToken) throws TokenRefreshException {
        String jwt = jwtService.generateToken(username);

        if (isExpired(refreshToken)) {
            repo.delete(refreshToken);
            throw new TokenRefreshException(refreshToken.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }

        TokenResponse response =
                new TokenResponse(jwt, jwtService.extractExpiration(jwt));
        response.setHeaders(getRefreshCookieHeader(refreshToken));

        return response;
    }


    private boolean isExpired(RefreshToken token) {
        boolean expired = token.getExpiryDate().compareTo(Instant.now()) <= 0;
        return expired;
    }

    public HttpHeaders getRefreshCookieHeader(RefreshToken refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", getRefreshCookieHeaderValue(refreshToken, "/refreshjwt"));
        headers.add("Set-Cookie", getRefreshCookieHeaderValue(refreshToken, "/logout"));
        return headers;
    }

    private String getRefreshCookieHeaderValue(RefreshToken refreshToken, String path) {
        return "refresh_token=" + refreshToken.getToken() + "; " +
                "Max-Age=" + refreshToken.getExpiryDate() + "; " +
                "Domain=localhost; Path=" + path + "; HttpOnly";
    }


}
