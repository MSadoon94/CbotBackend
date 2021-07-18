package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.token.services.TokenService;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.services.RefreshService;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class RefreshServiceIntTest {

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken("userId", UUID.randomUUID().toString(), Instant.now().plusSeconds(1800L));

    private static final TokenRequest TOKEN_REQUEST =
            new TokenRequest(REFRESH_TOKEN.getToken(), "username");

    private HttpHeaders mockHeader = new HttpHeaders();

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RefreshTokenRepository tokenRepo;

    private TokenService tokenService;

    private JwtService jwtService;

    private RefreshService refresher;

    @BeforeEach
    public void setUp() {
        setRepo();
        setMockHeader();
        setDependencies();
    }

    @Test
    void shouldAddNewJwtToTokenResponse() {
        String jwt = refresher.refresh(TOKEN_REQUEST, REFRESH_TOKEN.getToken()).getJwt();

        assertThat(jwtService.extractUsername(jwt),
                is(equalTo(TOKEN_REQUEST.getUsername())));
    }

    @Test
    void shouldAddHeaderWithSameValuesAsMockHeader() {
        assertThat(refresher.refresh(TOKEN_REQUEST, REFRESH_TOKEN.getToken()).getHeaders().entrySet(),
                is(equalTo(mockHeader.entrySet())));
    }

    private void setMockHeader() {
        mockHeader.add("Set-Cookie",
                "refresh_token=" + REFRESH_TOKEN.getToken() + "; " +
                        "Max-Age=" + REFRESH_TOKEN.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/refreshjwt; HttpOnly"
        );
    }

    private void setRepo() {
        tokenRepo.deleteAll();
        tokenRepo.save(REFRESH_TOKEN);
    }

    private void setDependencies() {
        jwtService = new JwtService(appProperties);
        tokenService = new TokenService(appProperties, tokenRepo);
        refresher = new RefreshService(tokenService, jwtService);
    }
}
