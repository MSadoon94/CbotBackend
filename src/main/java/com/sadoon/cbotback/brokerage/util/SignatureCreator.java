package com.sadoon.cbotback.brokerage.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SignatureCreator {

    private static final Logger logger = LoggerFactory.getLogger(SignatureCreator.class);

    public String requestWrapper(BrokerageDto request) {
        String update = request.getNonce() + formattedBody(request);
        return signature(update, request.getPassword(), request.getEndpoint());
    }

    public String signature(String update, String initKey, String macUpdate) {
        String signature = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((update.getBytes()));
            signature = new String(Base64.getEncoder().encode(
                    getPrimedHmac(initKey, macUpdate).doFinal(md.digest())
            ));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error while creating signature: ", e);
        }
        return signature;
    }

    private Mac getPrimedHmac(String initKey, String macUpdate) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance("HmacSHA512");
        hmac.init(new SecretKeySpec(Base64.getDecoder().decode(initKey.getBytes()), "HmacSHA512"));
        hmac.update(macUpdate.getBytes());
        return hmac;
    }

    private String formattedBody(BrokerageDto request) {
        String bodyValues = request.getBodyValues().toString();
        bodyValues = bodyValues.replaceAll("[{}\\[\\],]", "").trim();
        return bodyValues.replaceAll("[\\s]", "&");
    }
}
