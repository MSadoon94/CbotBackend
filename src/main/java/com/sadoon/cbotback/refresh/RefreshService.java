package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.exceptions.RefreshExpiredException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshService {

    private final JwtService jwtService;
    private final Long refreshTokenDurationMs;
    private  UserService userService;

    public RefreshService(AppProperties props, UserService userService, JwtService jwtService){
        this.refreshTokenDurationMs = props.getRefreshTokenDurationMs();
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public RefreshResponse refresh(Principal principal, String refreshToken) throws RefreshTokenNotFoundException, RefreshExpiredException, UserNotFoundException {
        return getResponse(
                principal,
                getRefreshToken(principal, refreshToken));
    }

    public RefreshToken createRefreshToken(String userId) throws UserNotFoundException {
        User user = userService.getUserWithId(userId);
        RefreshToken token = new RefreshToken(
                UUID.randomUUID().toString(),
                Instant.now().plusMillis(refreshTokenDurationMs)
        );
        user.setRefreshToken(token);
        userService.replace(user);
        return token;
    }

    public void deleteRefreshToken(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
        user.setRefreshToken(null);
        userService.replace(user);
    }

    public ResponseCookie getResponseCookie(Principal principal, String token, String path) throws UserNotFoundException, RefreshTokenNotFoundException {
        RefreshToken refreshToken = getRefreshToken(principal, token);

        return ResponseCookie
                .from("refresh_token", refreshToken.getToken())
                .httpOnly(true)
                .domain("localhost")
                .path(String.format("/api%s", path))
                .maxAge(Duration.between(Instant.now(), refreshToken.getExpiryDate()))
                .build();

    }

    private RefreshResponse getResponse(Principal principal, RefreshToken refreshToken) throws UserNotFoundException, RefreshExpiredException {
        String jwt = jwtService.generateToken(principal.getName());

        verifyExpiration(principal, refreshToken);

        return new RefreshResponse(jwt, jwtService.extractExpiration(jwt));
    }

    private RefreshToken verifyExpiration(Principal principal, RefreshToken token) throws RefreshExpiredException, UserNotFoundException {
        if (isExpired(token)) {
            User user = userService.getUserWithUsername(principal.getName());
            user.setRefreshToken(null);
            userService.replace(user);
            throw new RefreshExpiredException();
        }
        return token;
    }

    private RefreshToken getRefreshToken(Principal principal, String token) throws RefreshTokenNotFoundException, UserNotFoundException {
        RefreshToken userToken = userService.getUserWithUsername(principal.getName()).getRefreshToken();
        if(!token.equals(userToken.getToken())){
            throw new RefreshTokenNotFoundException(token);
        } else {
            return userToken;
        }
    }

    public HttpHeaders getRefreshCookieHeader(RefreshToken refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", getRefreshCookieHeaderValue(refreshToken, "/refresh-jwt"));
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
