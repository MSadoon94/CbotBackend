package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.exceptions.RefreshException;
import com.sadoon.cbotback.exceptions.RefreshExpiredException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class RefreshController {

    private static final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    private RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @PostMapping("/refresh-jwt")
    public ResponseEntity<RefreshResponse> refreshJwt(
            @CookieValue(name = "refresh_token") String tokenString,
            Principal principal) throws RefreshTokenNotFoundException, RefreshExpiredException {

        RefreshResponse refreshResponse = refreshService.refresh(principal, tokenString);
        return ResponseEntity.ok()
                .headers(refreshResponse.getHeaders())
                .body(refreshResponse);
    }

    @DeleteMapping("/log-out")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token") String tokenString) throws RefreshTokenNotFoundException, RefreshExpiredException {
        ResponseEntity<Void> response;
        try {
            if (getToken(tokenString) != null) {
                refreshService.deleteRefreshToken(getToken(tokenString));
                response = ResponseEntity.noContent().build();
            } else response = ResponseEntity.notFound().build();
        } catch (RefreshException e) {
            logger.error(e.getMessage(), e);
            response = ResponseEntity.notFound().build();
        }

        return response;
    }

    private RefreshToken getToken(String tokenString) throws RefreshTokenNotFoundException, RefreshExpiredException {
        return refreshService.verifyExpiration(refreshService.getRefreshToken(tokenString));
    }

}
