package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.exceptions.RefreshExpiredException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class RefreshController {

    private RefreshService refreshService;

    public RefreshController(RefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @PostMapping("/refresh-jwt")
    public ResponseEntity<RefreshResponse> refreshJwt(
            @CookieValue(name = "refresh_token") String tokenString,
            Principal principal) throws RefreshTokenNotFoundException, RefreshExpiredException, UserNotFoundException {

        RefreshResponse refreshResponse = refreshService.refresh(principal, tokenString);

        return ResponseEntity.ok()
                .headers(getRefreshHeaders(principal, tokenString))
                .body(refreshResponse);
    }

    @DeleteMapping("/log-out")
    public ResponseEntity<Void> logout(Principal principal) throws UserNotFoundException {

        refreshService.deleteRefreshToken(principal);

        return ResponseEntity.noContent().build();
    }

    private HttpHeaders getRefreshHeaders(Principal principal, String token) throws UserNotFoundException, RefreshTokenNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
                refreshService.getResponseCookie(principal, token, "/refresh-jwt").toString());
        headers.add(HttpHeaders.SET_COOKIE,
                refreshService.getResponseCookie(principal, token, "/log-out").toString());
        return headers;
    }

}
