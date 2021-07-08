package com.sadoon.cbotback.security.services;
import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class TokenServiceTest {

    @Autowired
    private RefreshTokenRepository tokenRepo;

    @Autowired
    private AppProperties appProperties;

    private TokenService tokenService;

    @BeforeEach
    public void setUp(){
        tokenRepo.deleteAll();
        tokenService = new TokenService(appProperties, tokenRepo);
    }

    @Test
    void shouldAddRefreshTokenToRepo(){
        RefreshToken token = tokenService.createRefreshToken("token");

        assertThat(tokenRepo.findById("token").orElse(null), samePropertyValuesAs(token));
    }

}
