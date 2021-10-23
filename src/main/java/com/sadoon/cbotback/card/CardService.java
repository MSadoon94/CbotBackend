package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.security.AESKeyUtil;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class CardService {

    private final AESKeyUtil aesKeyUtil;

    public CardService(AESKeyUtil aesKeyUtil) {
        this.aesKeyUtil = aesKeyUtil;
    }

    public Card newCard(CardApiRequest request) {
        return new Card(request.getCardName(), request.getPassword(), new Balances());
    }

    public Card encryptCard(Card card) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        String encrypted = aesKeyUtil.encrypt(
                "AES/GCM/NoPadding",
                card.getPassword(),
                aesKeyUtil.generateKey(256),
                aesKeyUtil.getIv()
        );
        card.setPassword(encrypted);
        return card;
    }

}
