package com.sadoon.cbotback.brokerage.util;

import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class SignatureCreator {

    public String getSignature(KrakenRequest request) {
        String signature = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update((request.getNonce() + formattedBody(request)).getBytes());

            signature = new String(Base64.getEncoder().encode(
                    getPrimedHmac(request).doFinal(md.digest())));

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return signature;
    }

    private String formattedBody(KrakenRequest request) {
        String bodyValues = request.getBodyValues().toString();
        bodyValues = bodyValues.replaceAll("[{}\\[\\],]", "").trim();
        return bodyValues.replaceAll("[\\s]", "&");
    }

    private Mac getPrimedHmac(KrakenRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA512");
        hmac.init(new SecretKeySpec(
                Base64.getDecoder()
                        .decode(request.getPassword().getBytes(StandardCharsets.UTF_8)), "HmacSHA512"));
        hmac.update(request.getEndpoint().getBytes());
        return hmac;
    }
}
