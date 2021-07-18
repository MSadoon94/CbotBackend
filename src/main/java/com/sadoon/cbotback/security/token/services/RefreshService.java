package com.sadoon.cbotback.security.token.services;

import com.sadoon.cbotback.security.token.TokenRefreshException;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public class RefreshService {

    private TokenService tokenService;
    private JwtService jwtService;

    public RefreshService(TokenService tokenService, JwtService jwtService) {
        this.tokenService = tokenService;
        this.jwtService = jwtService;
    }

    public TokenResponse refresh(TokenRequest request, String refreshToken) {

        return getResponse(
                request.getUsername(),
                getRefreshToken(refreshToken));
    }

    private RefreshToken getRefreshToken(String token) {
        return tokenService.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token is not in database!"));
    }

    private TokenResponse getResponse(String username, RefreshToken refreshToken) {
        String jwt = jwtService.generateToken(username);
        refreshToken = tokenService.verifyExpiration(refreshToken);

        TokenResponse response =
                new TokenResponse(jwt, jwtService.extractExpiration(jwt));
        response.setHeaders(tokenService.getRefreshCookieHeader(refreshToken));

        return response;
    }


}
