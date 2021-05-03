package com.sadoon.cbotback.security.refresh_token;

import com.sadoon.cbotback.security.token.TokenRefreshException;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import com.sadoon.cbotback.security.token.services.RefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RefreshController {

    private static final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    private RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @PostMapping("/refreshjwt")
    public ResponseEntity<TokenResponse> refreshJwt(
            @CookieValue(name = "refresh_token") String tokenString,
            @RequestBody TokenRequest request) {

        ResponseEntity<TokenResponse> response;

        try {

            if (getToken(tokenString) != null) {
                response = getTokenResponse(request, tokenString);
            } else response = ResponseEntity.notFound().build();

        } catch (TokenRefreshException e) {

            logger.error(e.getMessage(), e);
            TokenResponse tokenResponse = new TokenResponse(null, null);
            tokenResponse.setMessage(e.getMessage());
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(tokenResponse);

        }

        return response;
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token") String tokenString) {
        ResponseEntity<Void> response;
        try {
            if (getToken(tokenString) != null) {
                refreshService.deleteRefreshToken(getToken(tokenString));
                response = ResponseEntity.noContent().build();
            } else response = ResponseEntity.notFound().build();
        } catch (TokenRefreshException e) {
            logger.error(e.getMessage(), e);
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

    private ResponseEntity<TokenResponse> getTokenResponse(TokenRequest request, String refreshToken) {
        TokenResponse tokenResponse = refreshService.refresh(request, refreshToken);
        return ResponseEntity.ok()
                .headers(tokenResponse.getHeaders())
                .body(tokenResponse);
    }

    private RefreshToken getToken(String tokenString) throws TokenRefreshException {
        return refreshService.verifyExpiration(refreshService.getRefreshToken(tokenString));
    }

}
