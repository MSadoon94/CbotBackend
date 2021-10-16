package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.exceptions.RefreshException;
import com.sadoon.cbotback.refresh.models.RefreshRequest;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

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

    public RefreshResponse refresh(RefreshRequest request, String refreshToken) {

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

    public RefreshToken verifyExpiration(RefreshToken token) throws RefreshException {

        if (isExpired(token)) {
            repo.delete(token);
            throw new RefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new sign in request");
        }
        return token;
    }

    public RefreshToken getRefreshToken(String token) throws RefreshException {
        return repo.findByToken(token)
                .orElseThrow(() -> new RefreshException(token, "Refresh token is not in database!"));
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


    public HttpHeaders getRefreshCookieHeader(RefreshToken refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", getRefreshCookieHeaderValue(refreshToken, "/refreshjwt"));
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
