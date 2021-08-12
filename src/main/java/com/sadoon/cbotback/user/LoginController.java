package com.sadoon.cbotback.user;

import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok()
                .headers(response.getHeader())
                .body(getResponseWithHeaderFieldCleared(response));
    }


    /*
    The header field in the login response is cleared
    after it transports the header data to the ResponseEntity header.
    */
    private LoginResponse getResponseWithHeaderFieldCleared(LoginResponse response) {
        response.setHeader(null);
        return response;
    }

}
