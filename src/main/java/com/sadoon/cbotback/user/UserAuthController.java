package com.sadoon.cbotback.user;

import com.sadoon.cbotback.security.token.TokenRefreshException;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.services.RefreshService;
import com.sadoon.cbotback.security.token.services.TokenService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserAuthController {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthController.class);

    private AuthenticationManager authenticator;
    private TokenService tokenService;
    private JwtService jwtService;
    private LoginService loginService;
    private RefreshService refreshService;

    public UserAuthController(AuthenticationManager authenticator,
                              TokenService tokenService,
                              JwtService jwtService) {
        this.authenticator = authenticator;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
        setServices();
    }

    private void setServices() {
        this.loginService = new LoginService(authenticator, tokenService, jwtService);
        this.refreshService = new RefreshService(tokenService, jwtService);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login
            (@RequestBody LoginRequest request) {
        LoginResponse response = loginService.handleLogin(request);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok()
                .headers(response.getHeader())
                .body(getResponseWithHeaderFieldCleared(response));
    }

    @PostMapping("/refreshjwt")
    public ResponseEntity<TokenResponse> refreshJwt(
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestBody TokenRequest request) {

        ResponseEntity<TokenResponse> response;
        RefreshToken token;

        try {

            token = tokenService.verifyExpiration(getToken(refreshToken));

            if (token != null) {
                response = getResponse(request, refreshToken);
            } else response = ResponseEntity.notFound().build();

        } catch (TokenRefreshException e) {

            logger.error(e.getMessage(), e);
            TokenResponse tokenResponse = new TokenResponse(null, null);
            tokenResponse.setMessage(e.getMessage());
            response = ResponseEntity.badRequest().body(tokenResponse);

        }

        return response;
    }

    private RefreshToken getToken(String token) throws TokenRefreshException {
        return tokenService
                .findByToken(token)
                .orElseThrow(() ->
                        new TokenRefreshException(token, "Refresh token is not in database!"));
    }

    private ResponseEntity<TokenResponse> getResponse(TokenRequest request, String refreshToken) {
        TokenResponse tokenResponse = refreshService.refresh(request, refreshToken);
        return ResponseEntity.ok()
                .headers(tokenResponse.getHeaders())
                .body(tokenResponse);
    }


    private LoginResponse getResponseWithHeaderFieldCleared(LoginResponse response) {
        response.setHeader(null);
        return response;
    }

}
