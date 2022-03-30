package com.sadoon.cbotback.security;

import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.security.credentials.CredentialManager;
import com.sadoon.cbotback.security.credentials.CredentialsService;
import com.sadoon.cbotback.security.credentials.SecurityCredentials;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.user.UserService;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest {

    @Mock
    private CredentialManager manager;
    @Mock
    private UserService userService;

    private User mockUser = Mocks.user();
    private SecurityCredentials unencrypted =
            new SecurityCredentials(ExchangeType.KRAKEN.name(), "mockAccount", "mockPassword");

    private SecurityCredentials encrypted = new SecurityCredentials(
            ExchangeType.KRAKEN.name(), unencrypted.account(), "mockEncryptedPassword"
    );

    private CredentialsService service;

    @BeforeEach
    public void setUp() {
        service = new CredentialsService(manager, userService);
    }

    @Test
    void shouldAddEncryptedCredentialsToUserRepo() throws Exception {
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        given(manager.addCredentials(any(), any())).willReturn(encrypted.password());

        service.addCredentials(mockUser.getUsername(), unencrypted);

        assertThat(mockUser.getCredential(ExchangeType.KRAKEN.name()), samePropertyValuesAs(encrypted));
    }

    @Test
    void shouldReturnDecryptedCredentials() throws Exception {
        mockUser.setCredential(encrypted.type(), encrypted);
        given(userService.getUserWithUsername(any())).willReturn(mockUser);
        given(manager.decryptPassword(any(), any())).willReturn(unencrypted.password());

        assertThat(service.getCredentials(mockUser.getUsername(), unencrypted.type()), samePropertyValuesAs(unencrypted));
    }

}
