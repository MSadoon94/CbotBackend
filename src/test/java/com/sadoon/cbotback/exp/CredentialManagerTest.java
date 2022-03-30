package com.sadoon.cbotback.exp;

import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.security.credentials.CredentialManager;
import com.sadoon.cbotback.security.credentials.SecurityCredentials;
import com.sadoon.cbotback.security.util.AESKeyUtil;
import com.sadoon.cbotback.security.util.KeyStoreUtil;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.spec.SecretKeySpec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CredentialManagerTest {

    @Mock
    private AESKeyUtil aesKeyUtil;
    @Mock
    private SignatureCreator signatureCreator;
    @Mock
    private KeyStoreUtil keyStoreUtil;

    private SecurityCredentials mockCredentials =
            new SecurityCredentials(ExchangeType.KRAKEN.name(), "mockAccount", "mockPassword");

    private SecurityCredentials encryptedCredentials =
            new SecurityCredentials(ExchangeType.KRAKEN.name(), "mockAccount", "mockEncrypted");

    @InjectMocks
    private CredentialManager manager;

    @Test
    void shouldReturnCredentialsOnSuccessfulAdd() throws Exception {
        given(aesKeyUtil.getIv()).willReturn("mockIv".getBytes());
        given(aesKeyUtil.encrypt(any(), any(), any(), any())).willReturn(encryptedCredentials.password());

        assertThat(manager.addCredentials(Mocks.user().getUsername(), mockCredentials), is(encryptedCredentials.password()));
    }

    @Test
    void shouldReturnCredentialsOnSuccessfulPasswordVerification() throws Exception {
        given(keyStoreUtil.getKey(any())).willReturn(new SecretKeySpec("mockKey".getBytes(), "AES"));
        given(aesKeyUtil.decrypt(any(), any(), any(), any())).willReturn(mockCredentials.password());

        assertThat(manager.decryptPassword(encryptedCredentials, Mocks.user().getUsername()), is(mockCredentials.password()));

    }

}
