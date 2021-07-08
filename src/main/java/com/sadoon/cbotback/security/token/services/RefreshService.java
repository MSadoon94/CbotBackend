package com.sadoon.cbotback.security.token.services;

import com.sadoon.cbotback.security.token.TokenRefreshException;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class RefreshService {

    private TokenService tokenService;
    private JwtService jwtService;

    public RefreshService(TokenService tokenService, JwtService jwtService) {
        this.tokenService = tokenService;
        this.jwtService = jwtService;
    }

    public TokenResponse refresh(TokenRequest request){

        return getResponse(
                request.getUsername(),
                getRefreshToken(request.getRefreshToken()));
    }

    private RefreshToken getRefreshToken(String token){
        return tokenService.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(token, "Refresh token is not in database!"));
    }

    private TokenResponse getResponse(String username, RefreshToken refreshToken){

        TokenResponse response = new TokenResponse(
                jwtService.generateToken(username),
                tokenService.verifyExpiration(refreshToken).getToken());
        response.setHeaders(getRefreshCookieHeader(refreshToken));

        return response;
    }

    private HttpHeaders getRefreshCookieHeader(RefreshToken refreshToken){
        HttpHeaders headers = new HttpHeaders();
        headers.add(
                "Set-Cookie",
                "refresh_token=" + refreshToken.getToken() + "; " +
                        "Max-Age=" + refreshToken.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/home; HttpOnly");
        return headers;
    }

}
