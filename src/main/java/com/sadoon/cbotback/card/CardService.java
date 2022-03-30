package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.notfound.CardNotFoundException;
import com.sadoon.cbotback.exceptions.password.PasswordException;
import com.sadoon.cbotback.security.util.AESKeyUtil;
import com.sadoon.cbotback.security.util.KeyStoreUtil;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Optional;

@Service
public class CardService {

    private final AESKeyUtil aesKeyUtil;
    private final KeyStoreUtil keyStoreUtil;
    private final SignatureCreator signatureCreator;

    public CardService(AESKeyUtil aesKeyUtil, KeyStoreUtil keyStoreUtil, SignatureCreator signatureCreator) {
        this.aesKeyUtil = aesKeyUtil;
        this.keyStoreUtil = keyStoreUtil;
        this.signatureCreator = signatureCreator;
    }

    public Card newCard(CardApiRequest request) {
        return new Card(request.getCardName(), request.getPassword(), new Balances());
    }

    public Card getCard(User user, String name) throws CardNotFoundException {
        return Optional.ofNullable(user.getCards().get(name))
                .orElseThrow(() -> new CardNotFoundException(name));
    }

    public Card encryptCard(Card card, Principal principal) throws GeneralSecurityException, IOException {

        SecretKey key = aesKeyUtil.generateKey(256);
        SecretKey iv = new SecretKeySpec(aesKeyUtil.getIv(), "AES");
        String signature = signatureCreator.signature(card.getCardName(), card.getPassword(), principal.getName());

        String encrypted = aesKeyUtil.encrypt(
                "AES/GCM/NoPadding",
                card.getPassword(),
                key,
                iv.getEncoded()
        );
        card.setPassword(encrypted);

        keyStoreUtil.storeSecretKeyEntry(key, String.format("%skey", signature));
        keyStoreUtil.storeSecretKeyEntry(iv, String.format("%siv", signature));
        return card;
    }


    public void verifyPassword(Card card, String password, Principal principal)
            throws PasswordException {

        if(password.length() < 6){
            throw new PasswordException("Password cannot be shorter than 6 characters.");
        }

        String signature = signatureCreator.signature(card.getCardName(), password, principal.getName());
        String decrypted;
        try {
            SecretKey key = (SecretKey) keyStoreUtil.getKey(String.format("%skey", signature));
            SecretKey iv = (SecretKey) keyStoreUtil.getKey(String.format("%siv", signature));

            decrypted = aesKeyUtil.decrypt(
                    "AES/GCM/NoPadding",
                    card.getPassword(),
                    key,
                    iv.getEncoded()
            );
        } catch (Exception e) {
            throw new PasswordException("card", new ApiError(HttpStatus.BAD_REQUEST, e.getMessage(), e));
        }

        if (!decrypted.equals(password)) {
            throw new PasswordException("card");
        }
    }

}
