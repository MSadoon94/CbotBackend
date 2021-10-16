package com.sadoon.cbotback.user;

import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class LoginController {

    private final LoginService loginService;


    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login
            (@RequestBody LoginRequest request) {
        LoginResponse response = loginService.handleLogin(request);

        if (response == null) {
            response = new LoginResponse("", "", new Date(System.currentTimeMillis()));
            response.setIsLoggedIn(false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setIsLoggedIn(true);
        return ResponseEntity.ok()
                .headers(loginService.getHeader(request.getUserId()))
                .body(response);
    }
}
