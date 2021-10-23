package com.sadoon.cbotback.security;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class AESKeyUtilTest {

    @Test
    void shouldReturnEncryptedStringThatDecryptsToSameInput() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        AESKeyUtil aesKeyUtil = new AESKeyUtil();

        String algorithm = "AES/GCM/NoPadding";
        String input = "pass";
        SecretKey secretKey = aesKeyUtil.generateKey(256);

        byte[] IV = aesKeyUtil.getIv();

        String encrypted = aesKeyUtil.encrypt(algorithm, input, secretKey, IV);
        String decrypted = aesKeyUtil.decrypt(algorithm, encrypted, secretKey, IV);

        assertThat(decrypted, is(equalTo(input)));
    }
}
