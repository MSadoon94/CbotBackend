package com.sadoon.cbotback.security.credentials;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.security.util.SignatureCreator;
import com.sadoon.cbotback.security.util.AESKeyUtil;
import com.sadoon.cbotback.security.util.KeyStoreUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class CredentialsConfig {

    @Bean
    KeyStoreUtil keyStoreUtil(AppProperties props) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        return new KeyStoreUtil(props);
    }

    @Bean
    CredentialManager credentialManager(KeyStoreUtil keyStoreUtil) {
        return new CredentialManager(
                new AESKeyUtil(),
                new SignatureCreator(),
                keyStoreUtil
                );
    }
}
