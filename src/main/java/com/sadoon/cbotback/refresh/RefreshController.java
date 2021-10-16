package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.exceptions.RefreshException;
import com.sadoon.cbotback.refresh.models.RefreshRequest;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.refresh.models.RefreshToken;
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
    public ResponseEntity<RefreshResponse> refreshJwt(
            @CookieValue(name = "refresh_token") String tokenString,
            @RequestBody RefreshRequest request) {

        ResponseEntity<RefreshResponse> response;

        try {

            if (getToken(tokenString) != null) {
                response = getTokenResponse(request, tokenString);
            } else response = ResponseEntity.notFound().build();

        } catch (RefreshException e) {

            logger.error(e.getMessage(), e);
            RefreshResponse refreshResponse = new RefreshResponse(null, null);
            refreshResponse.setMessage(e.getMessage());
            response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(refreshResponse);

        }

        return response;
    }

    @DeleteMapping("/log-out")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token") String tokenString) {
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

    private ResponseEntity<RefreshResponse> getTokenResponse(RefreshRequest request, String refreshToken) {
        RefreshResponse refreshResponse = refreshService.refresh(request, refreshToken);
        return ResponseEntity.ok()
                .headers(refreshResponse.getHeaders())
                .body(refreshResponse);
    }

    private RefreshToken getToken(String tokenString) throws RefreshException {
        return refreshService.verifyExpiration(refreshService.getRefreshToken(tokenString));
    }

}
