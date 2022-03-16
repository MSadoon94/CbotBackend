package com.sadoon.cbotback.exp;

import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exceptions.password.CredentialsException;
import com.sadoon.cbotback.exceptions.password.PasswordException;
import com.sadoon.cbotback.exchange.model.ExchangeCredentials;
import com.sadoon.cbotback.security.AESKeyUtil;
import com.sadoon.cbotback.security.KeyStoreUtil;
import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
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

    private Authentication mockAuth = Mocks.auth(Mocks.user());

    private ExchangeCredentials mockCredentials =
            new ExchangeCredentials("mockAccount", "mockPassword");

    private ExchangeCredentials encryptedCredentials =
            new ExchangeCredentials("mockAccount", "mockEncrypted");

    @InjectMocks
    private CredentialManager manager;

    @Test
    void shouldReturnCredentialsOnSuccessfulAdd() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, CertificateException, KeyStoreException, IOException {
        given(aesKeyUtil.getIv()).willReturn("mockIv".getBytes());
        given(aesKeyUtil.encrypt(any(), any(), any(), any())).willReturn(encryptedCredentials.password());

        assertThat(manager.addCredentials(mockAuth, mockCredentials), samePropertyValuesAs(encryptedCredentials));
    }

    @Test
    void shouldReturnCredentialsOnSuccessfulPasswordVerification() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, PasswordException, CredentialsException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        given(keyStoreUtil.getKey(any())).willReturn(new SecretKeySpec("mockKey".getBytes(), "AES"));
        given(aesKeyUtil.decrypt(any(), any(), any(), any())).willReturn(mockCredentials.password());

        assertThat(manager.decryptPassword(encryptedCredentials, mockAuth), samePropertyValuesAs(mockCredentials));

    }



}
