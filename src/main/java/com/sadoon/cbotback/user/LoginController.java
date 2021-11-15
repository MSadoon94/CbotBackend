package com.sadoon.cbotback.user;

import com.sadoon.cbotback.exceptions.LoginCredentialsException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.exceptions.UnauthorizedUserException;
import com.sadoon.cbotback.exceptions.UserNotFoundException;
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

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login
            (@RequestBody LoginRequest request) throws UserNotFoundException,
            LoginCredentialsException, UnauthorizedUserException, RefreshTokenNotFoundException {

        Principal principal = loginService.getPrincipal(request);

        LoginResponse response =
                Optional.ofNullable(loginService.handleLogin(request))
                        .orElseThrow(LoginCredentialsException::new);

        response.setIsLoggedIn(true);
        return ResponseEntity.ok()
                .headers(loginService.getHeaders(principal))
                .body(response);
    }
}
