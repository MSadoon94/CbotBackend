package com.sadoon.cbotback.security;

import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.password.CredentialsException;
import com.sadoon.cbotback.exceptions.password.PasswordException;
import org.springframework.http.HttpStatus;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class CredentialManager {

    private AESKeyUtil aesKeyUtil;
    private SignatureCreator signatureCreator;
    private KeyStoreUtil keyStoreUtil;

    public CredentialManager(AESKeyUtil aesKeyUtil,
                             SignatureCreator signatureCreator,
                             KeyStoreUtil keyStoreUtil
    ) {
        this.aesKeyUtil = aesKeyUtil;
        this.signatureCreator = signatureCreator;
        this.keyStoreUtil = keyStoreUtil;
    }


    public String addCredentials(Principal principal, SecurityCredentials credentials) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, CertificateException, KeyStoreException, IOException {
        SecretKey key = aesKeyUtil.generateKey(256);
        SecretKey iv = new SecretKeySpec(aesKeyUtil.getIv(), "AES");

        String signature = signatureCreator.signature(
                credentials.account(),
                credentials.password(),
                principal.getName()
        );

        String encrypted = aesKeyUtil.encrypt(
                "AES/GCM/NoPadding",
                credentials.password(),
                key,
                iv.getEncoded()
        );

        keyStoreUtil.storeSecretKeyEntry(key, String.format("%1skey", signature));
        keyStoreUtil.storeSecretKeyEntry(iv, String.format("%siv", signature));
        return encrypted;
    }

    public String decryptPassword(SecurityCredentials credentials, Principal principal) throws PasswordException, CredentialsException {
        if (credentials.password().length() < 6) {
            throw new CredentialsException(
                    "Password",
                    new ApiError(HttpStatus.UNAUTHORIZED, "Password cannot be shorter than 6 characters."));
        }

        String signature = signatureCreator.signature(
                credentials.account(),
                credentials.password(),
                principal.getName()
        );
        String decryptedPass;
        try {
            SecretKey password = (SecretKey) keyStoreUtil.getKey(String.format("%skey", signature));
            SecretKey iv = (SecretKey) keyStoreUtil.getKey(String.format("%siv", signature));

            decryptedPass = aesKeyUtil.decrypt(
                    "AES/GCM/NoPadding",
                    credentials.password(),
                    password,
                    iv.getEncoded()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasswordException("credentials", new ApiError(HttpStatus.BAD_REQUEST, e.getMessage(), e));
        }

        if (!decryptedPass.equals(credentials.password())) {
            throw new CredentialsException("Exchange password");
        }

        return decryptedPass;
    }

}
