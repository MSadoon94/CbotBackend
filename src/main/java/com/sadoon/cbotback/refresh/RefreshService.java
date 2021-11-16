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
import org.springframework.stereotype.Service;

import java.security.Principal;
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

    public RefreshToken createRefreshToken(Principal principal) throws UserNotFoundException {
        User user = userService.getUserWithUsername(principal.getName());
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

    public RefreshToken getRefreshToken(Principal principal, String token) throws RefreshTokenNotFoundException, UserNotFoundException {
        RefreshToken userToken = userService.getUserWithUsername(principal.getName()).getRefreshToken();
        if(!token.equals(userToken.getToken())){
            throw new RefreshTokenNotFoundException(token);
        } else {
            return userToken;
        }
    }

    private boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) <= 0;
    }

}
