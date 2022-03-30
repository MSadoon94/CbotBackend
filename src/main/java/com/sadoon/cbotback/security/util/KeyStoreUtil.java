package com.sadoon.cbotback.security.util;

import com.sadoon.cbotback.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

@Configuration
public class KeyStoreUtil {

    private AppProperties appProps;
    private KeyStore keyStore;
    private KeyStore.ProtectionParameter protectionParameter;
    private char[] pwdArray;
    private Path keystoreFilePath;

    public KeyStoreUtil(AppProperties appProps) {
        this.appProps = appProps;
    }

    @Bean
    void setUpKeystore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        keystoreFilePath = getKeystorePath();
        keyStore = KeyStore.getInstance("PKCS12");
        pwdArray = appProps.getKeystorePassword().toCharArray();

        if (Files.notExists(keystoreFilePath)) {
            keyStore.load(null, pwdArray);
            writeToFile();
        } else {
            try (InputStream inputStream = Files.newInputStream(keystoreFilePath)) {
                keyStore.load(inputStream, pwdArray);
            }
        }
        protectionParameter = new KeyStore.PasswordProtection(pwdArray);
    }


    public void storeSecretKeyEntry(SecretKey secretKey, String alias) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
        writeToFile();
    }

    public Key getKey(String alias) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException {
        InputStream inputStream = Files.newInputStream(keystoreFilePath);
        keyStore.load(inputStream, pwdArray);

        keyStore.aliases().asIterator().forEachRemaining(System.out::println);
        Key key = keyStore.getKey(alias, appProps.getKeystorePassword().toCharArray());

        inputStream.close();

        return key;
    }

    private Path getKeystorePath() throws IOException {
        Path keystorePath = Paths.get("./keystore");
        if (!Files.isDirectory(keystorePath)) {
            Files.createDirectories(keystorePath);
        }
        return keystorePath.resolve(String.format("%s.pfx", appProps.getKeystoreName()));
    }

    private void writeToFile() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        try (FileOutputStream fos = new FileOutputStream(keystoreFilePath.toString())) {
            keyStore.store(fos, pwdArray);
        }
    }
}

