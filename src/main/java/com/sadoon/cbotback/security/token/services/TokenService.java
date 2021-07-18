package com.sadoon.cbotback.security.token.services;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.TokenRefreshException;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository tokenRepo;

    public TokenService(AppProperties props, RefreshTokenRepository tokenRepo) {
        this.refreshTokenDurationMs = props.getRefreshTokenDurationMs();
        this.tokenRepo = tokenRepo;
    }

    public Optional<RefreshToken> findByToken(String refreshToken) {
        return tokenRepo.findByToken(refreshToken);
    }

    public RefreshToken createRefreshToken(String userId) {
        return tokenRepo.save(new RefreshToken(userId, UUID.randomUUID().toString(), Instant.now().plusMillis(refreshTokenDurationMs)));
    }

    public RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException {

        if (isExpired(token)) {
            tokenRepo.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    private boolean isExpired(RefreshToken token) {
        boolean expired = token.getExpiryDate().compareTo(Instant.now()) <= 0;
        return expired;
    }

    public HttpHeaders getRefreshCookieHeader(RefreshToken refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
                "Set-Cookie",
                "refresh_token=" + refreshToken.getToken() + "; " +
                        "Max-Age=" + refreshToken.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/refreshjwt; HttpOnly");
        return headers;
    }
}
