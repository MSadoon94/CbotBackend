package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.exceptions.RefreshException;
import com.sadoon.cbotback.refresh.RefreshService;
import com.sadoon.cbotback.refresh.RefreshTokenRepository;
import com.sadoon.cbotback.refresh.models.RefreshRequest;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
public class RefreshServiceIntTest {

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken("userId", UUID.randomUUID().toString(), Instant.now().plusSeconds(1800L));

    private static final RefreshRequest TOKEN_REQUEST =
            new RefreshRequest("username");

    private final HttpHeaders mockHeader = new HttpHeaders();

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RefreshTokenRepository tokenRepo;

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

    @Test
    void shouldDeleteTokensFromRepo() {
        refresher.deleteRefreshToken(REFRESH_TOKEN);
        try {
            refresher.getRefreshToken(REFRESH_TOKEN.getToken());
        } catch (Exception exception) {
            assertThat(exception, isA(RefreshException.class));
        }
    }

    @Test
    void shouldAddRefreshTokenToRepo() {
        RefreshToken token = refresher.createRefreshToken("token");

        assertThat(tokenRepo.findById("token").orElse(null), samePropertyValuesAs(token));
    }

    @Test
    void shouldReturnHeaderForTokenRequestsWithSameValuesAsMockHeader() {
        setMockHeader();

        assertThat(refresher.getRefreshCookieHeader(REFRESH_TOKEN).entrySet(),
                samePropertyValuesAs(mockHeader.entrySet()));
    }

    private void setMockHeader() {
        mockHeader.add("Set-Cookie",
                "refresh_token=" + REFRESH_TOKEN.getToken() + "; " +
                        "Max-Age=" + REFRESH_TOKEN.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/api/refreshjwt; HttpOnly"
        );
        mockHeader.add("Set-Cookie",
                "refresh_token=" + REFRESH_TOKEN.getToken() + "; " +
                        "Max-Age=" + REFRESH_TOKEN.getExpiryDate() + "; " +
                        "Domain=localhost; Path=/api/log-out; HttpOnly");
    }

    private void setRepo() {
        tokenRepo.deleteAll();
        tokenRepo.save(REFRESH_TOKEN);
    }

    private void setDependencies() {
        jwtService = new JwtService(appProperties);
        refresher = new RefreshService(appProperties, tokenRepo, jwtService);
    }
}
