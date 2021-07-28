package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.refresh_token.RefreshController;
import com.sadoon.cbotback.security.token.RefreshTokenRepository;
import com.sadoon.cbotback.security.token.models.RefreshToken;
import com.sadoon.cbotback.security.token.models.TokenRequest;
import com.sadoon.cbotback.security.token.models.TokenResponse;
import com.sadoon.cbotback.security.token.services.JwtService;
import com.sadoon.cbotback.security.token.services.RefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@SpringBootTest
@ActiveProfiles("test")
public class RefreshControllerIntTest {

    private static final RefreshToken REFRESH_TOKEN =
            new RefreshToken("userId", UUID.randomUUID().toString(), Instant.now().plusSeconds(1800L));

    private static final RefreshToken UNKNOWN_TOKEN =
            new RefreshToken("unknown", UUID.randomUUID().toString(), Instant.now().plusSeconds(1800L));

    private static final TokenRequest TOKEN_REQUEST =
            new TokenRequest("jwt", "username");


    @Autowired
    private AppProperties props;

    @Autowired
    private RefreshTokenRepository repo;

    private JwtService jwtService;

    private RefreshService refreshService;

    private RefreshController controller;

    @BeforeEach
    void setUp() {
        setRepo();
        jwtService = new JwtService(props);
        refreshService = new RefreshService(props, repo, jwtService);
        controller = new RefreshController(refreshService);
    }

    @Test
    void shouldReturnResponseEntityForRefreshRequests() {
        assertThat(controller.refreshJwt(REFRESH_TOKEN.getToken(), TOKEN_REQUEST), isA(ResponseEntity.class));
    }

    @Test
    void shouldAddTokenResponseToResponseEntityForRefreshRequests() {
        assertThat(controller.refreshJwt(REFRESH_TOKEN.getToken(), TOKEN_REQUEST).getBody(), isA(TokenResponse.class));
    }

    @Test
    void shouldReturnResponseEntityWithNotFoundStatusForTokensNotInDatabase() {
        HttpStatus responseStatus = controller.refreshJwt(UNKNOWN_TOKEN.getToken(), TOKEN_REQUEST).getStatusCode();

        assertThat(responseStatus, is(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldReturnSuccessfulResponseEntityForValidLogoutRequests() {
        assertThat(controller.logout(REFRESH_TOKEN.getToken()).getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    void shouldReturnFailedResponseEntityForInvalidLogoutRequests() {
        assertThat(controller.logout(UNKNOWN_TOKEN.getToken()).getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    private void setRepo() {
        repo.deleteAll();
        repo.save(REFRESH_TOKEN);
    }
}
