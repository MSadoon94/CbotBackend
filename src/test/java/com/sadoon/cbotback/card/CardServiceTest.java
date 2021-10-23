package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.security.AESKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private AESKeyUtil aesKeyUtil;

    private CardService cardService;


    @BeforeEach
    void setup() {
        cardService = new CardService(aesKeyUtil);
    }

    @Test
    void shouldCreateCardWithAllFieldsSet() {
        Card mockCard = new Card("mockName", "mockPassword", new Balances());
        assertThat(cardService.newCard(Mocks.cardRequest("brokerage")), samePropertyValuesAs(mockCard, "balances"));
    }

    @Test
    void shouldEncryptCardPassword() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        given(aesKeyUtil.encrypt(any(), any(), any(), any())).willReturn("mockEncrypted");

        String password = cardService.encryptCard(Mocks.card()).getPassword();
        assertThat(password, is(equalTo("mockEncrypted")));
    }

}