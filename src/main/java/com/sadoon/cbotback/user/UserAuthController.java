package com.sadoon.cbotback.user;

import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.services.RefreshService;
import com.sadoon.cbotback.security.token.services.TokenService;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthController {

    private AuthenticationManager authenticator;
    private TokenService tokenService;
    private JwtService jwtService;
    private LoginService loginService;
    private RefreshService refreshService;

    public UserAuthController(AuthenticationManager authenticator,
                              TokenService tokenService,
                              JwtService jwtService ) {
        this.authenticator = authenticator;
        this.tokenService = tokenService;
        this.jwtService = jwtService;
        setServices();
    }

    private void setServices(){
        this.loginService = new LoginService(authenticator,tokenService, jwtService);
        this.refreshService = new RefreshService(tokenService, jwtService);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponse> login
            (@RequestBody LoginRequest request) {
        LoginResponse response = loginService.handleLogin(request);

        if(response == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok()
                .body(response);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody TokenRequest request){
        TokenResponse response = refreshService.refresh(request);
        return ResponseEntity.ok()
                .headers(response.getHeaders())
                .body(response);
    }

}
