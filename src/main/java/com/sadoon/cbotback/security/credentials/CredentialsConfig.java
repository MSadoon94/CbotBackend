package com.sadoon.cbotback.security.credentials;

import com.sadoon.cbotback.AppProperties;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.security.util.AESKeyUtil;
import com.sadoon.cbotback.security.util.KeyStoreUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CredentialsConfig {

    @Bean
    CredentialManager credentialManager(AppProperties props) {
        return new CredentialManager(
                new AESKeyUtil(),
                new SignatureCreator(),
                new KeyStoreUtil(props)
        );
    }
}
