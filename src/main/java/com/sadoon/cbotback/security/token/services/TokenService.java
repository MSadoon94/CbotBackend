package com.sadoon.cbotback.security.token.services;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.TokenRefreshException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository tokenRepo;

    public TokenService(AppProperties props, RefreshTokenRepository tokenRepo) {
        this.refreshTokenDurationMs = props.getRefreshTokenDurationMs();
        this.tokenRepo = tokenRepo;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    public RefreshToken createRefreshToken(String userId){
        return tokenRepo.save(new RefreshToken(userId, Instant.now().plusMillis(refreshTokenDurationMs)));
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            tokenRepo.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
