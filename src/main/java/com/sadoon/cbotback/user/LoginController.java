package com.sadoon.cbotback.user;

import com.sadoon.cbotback.api.CookieService;
import com.sadoon.cbotback.exceptions.password.LoginCredentialsException;
import com.sadoon.cbotback.exceptions.notfound.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.notfound.UserNotFoundException;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
public class LoginController {

    private LoginService loginService;
    private CookieService cookieService;

    public LoginController(LoginService loginService, CookieService cookieService) {
        this.loginService = loginService;
        this.cookieService = cookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login
            (@RequestBody LoginRequest request) throws UserNotFoundException,
            LoginCredentialsException, RefreshTokenNotFoundException {

        Principal principal = loginService.getPrincipal(request);

        LoginResponse response =
                Optional.ofNullable(loginService.handleLogin(request))
                        .orElseThrow(LoginCredentialsException::new);

        response.setIsLoggedIn(true);

        return ResponseEntity.ok()
                .headers(cookieService.getJwtHeaders(principal, cookieService.getRefreshHeaders(principal)))
                .body(response);
    }
}
