package com.sadoon.cbotback.common;

import com.sadoon.cbotback.asset.AssetPairRequest;
import com.sadoon.cbotback.brokerage.model.Brokerage;
import com.sadoon.cbotback.brokerage.util.BrokerageDto;
import com.sadoon.cbotback.card.Card;
import com.sadoon.cbotback.home.models.CardApiRequest;
import com.sadoon.cbotback.user.models.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Mocks {

    public static User user() {
        return new User("mockUser", "mockPassword", new SimpleGrantedAuthority("USER"));
    }

    public static List<Card> cardList() {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("mockCard1", Map.of("USD", new BigDecimal("100"))));
        cards.add(new Card("mockCard2", Map.of("USD", new BigDecimal("50"))));
        return cards;
    }


    public static Card card() {
        return new Card("mockCard3", Map.of("USD", new BigDecimal("150")));
    }


    public static CardApiRequest cardRequest(String brokerage) {
        CardApiRequest request = new CardApiRequest("mockAccount", "mockPassword");
        request.setCardName("mockName");
        request.setBrokerage(brokerage);
        return request;
    }

    public static Authentication auth(User mockUser) {
        return new UsernamePasswordAuthenticationToken(mockUser, "mockPassword", mockUser.getAuthorities());
    }

    public static BrokerageDto brokerageDTO(String requestType, String url) {
        BrokerageDto dto = new BrokerageDto(cardRequest(brokerage(url).getName()), requestType);
        dto.setBrokerage(brokerage(url));
        dto.setNonce("mockNonce");
        return dto;
    }

    public static Brokerage brokerage(String url) {
        Brokerage brokerage = new Brokerage();
        brokerage.setName("mockKraken");
        brokerage.setUrl(url);
        brokerage.setEndpoints(Map.of("balance", "/mockBalance"));
        brokerage.setMethods(Map.of("balance", "POST"));
        brokerage.setSuccessKey("result");
        return brokerage;
    }

    public static AssetPairRequest assetPairRequest() {
        AssetPairRequest request = new AssetPairRequest();
        request.setAssets("BTCUSD");
        request.setBrokerage("kraken");
        return request;
    }

    public static Map<String, Map<String, String>> clientResponse() {
        return Map.of("result", Map.of("usd", "130"));
    }

}