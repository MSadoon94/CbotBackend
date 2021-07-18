package com.sadoon.cbotback.security.services;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class TokenServiceTest {

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken("userId", UUID.randomUUID().toString(), Instant.now().plusSeconds(1800L));

    private HttpHeaders mockHeader = new HttpHeaders();

    @Autowired
    private RefreshTokenRepository tokenRepo;

    @Autowired
    private AppProperties appProperties;

    private TokenService tokenService;

    @BeforeEach
    public void setUp() {
        tokenRepo.deleteAll();
        tokenService = new TokenService(appProperties, tokenRepo);
    }

    @Test
    void shouldAddRefreshTokenToRepo() {
        RefreshToken token = tokenService.createRefreshToken("token");

        assertThat(tokenRepo.findById("token").orElse(null), samePropertyValuesAs(token));
    }

    @Test
    void shouldReturnHeaderForTokenRequestsWithSameValuesAsMockHeader() {
        setMockHeader();

        assertThat(tokenService.getRefreshCookieHeader(REFRESH_TOKEN).entrySet(),
                samePropertyValuesAs(mockHeader.entrySet()));
    }

    private void setMockHeader() {
        mockHeader.add("Set-Cookie",
                "refresh_token=" + REFRESH_TOKEN.getToken() + "; " +
                        "Max-Age=" + REFRESH_TOKEN.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/home; HttpOnly"
        );
    }

}
