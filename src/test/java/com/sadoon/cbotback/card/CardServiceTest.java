package com.sadoon.cbotback.card;

import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.exceptions.not_found.CardNotFoundException;
import com.sadoon.cbotback.exceptions.password.PasswordException;
import com.sadoon.cbotback.security.AESKeyUtil;
import com.sadoon.cbotback.security.KeyStoreUtil;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private AESKeyUtil aesKeyUtil;
    @Mock
    private KeyStoreUtil keyStoreUtil;
    @Mock
    private SignatureCreator signatureCreator;

    private CardService cardService;

    private User mockUser = Mocks.user();
    private Card mockCard = Mocks.card();


    @BeforeEach
    void setup() {
        cardService = new CardService(aesKeyUtil, keyStoreUtil, signatureCreator);
    }

    @Test
    void shouldCreateCardWithAllFieldsSet() {
        assertThat(cardService.newCard(Mocks.cardRequest("brokerage")),
                samePropertyValuesAs(Mocks.card(), "balances"));
    }

    @Test
    void shouldEncryptCardPassword() throws Exception {
        given(aesKeyUtil.encrypt(any(), any(), any(), any())).willReturn("mockEncrypted");
        given(aesKeyUtil.getIv()).willReturn("mockIv".getBytes());

        String password = cardService.encryptCard(Mocks.card(), Mocks.auth(Mocks.user())).getPassword();
        assertThat(password, is(equalTo("mockEncrypted")));
    }

    @Test
    void shouldVerifyPasswordMatches() throws Exception {
        given(aesKeyUtil.decrypt(any(), any(), any(), any())).willReturn("mockPassword");
        given(keyStoreUtil.getKey(any())).willReturn(new SecretKeySpec("mockKey".getBytes(), "AES"));

        assertDoesNotThrow(() ->
                cardService.verifyPassword(Mocks.card(), "mockPassword", Mocks.auth(Mocks.user())));
    }

    @Test
    void shouldThrowExceptionIfPasswordIsNotSameAsDecryptedPassword() throws Exception {
        given(aesKeyUtil.decrypt(any(), any(), any(), any())).willReturn("wrongPassword");
        given(keyStoreUtil.getKey(any())).willReturn(new SecretKeySpec("mockKey".getBytes(), "AES"));

        assertThrows(PasswordException.class,
                () -> cardService.verifyPassword(Mocks.card(), "mockPassword", Mocks.auth(Mocks.user())));
    }

    @Test
    void shouldGetCardFromUserByName() throws CardNotFoundException {
        mockUser.setCards(Map.of(mockCard.getCardName(), mockCard));

        assertThat(cardService.getCard(mockUser, mockCard.getCardName()), samePropertyValuesAs(mockCard));
    }

    @Test
    void shouldThrowExceptionWhenCardCannotBeFound() {
        assertThrows(CardNotFoundException.class, () -> cardService.getCard(mockUser, mockCard.getCardName()));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsShorterThanSixChars(){
        assertThrows(PasswordException.class,
                ()-> cardService.verifyPassword(mockCard, "pass",  Mocks.auth(Mocks.user())));
    }

}