package com.sadoon.cbotback.common;

import com.sadoon.cbotback.api.PublicRequestDto;
import com.sadoon.cbotback.asset.AssetPair;
import com.sadoon.cbotback.asset.AssetPairRequest;
import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.brokerage.model.Balances;
import com.sadoon.cbotback.brokerage.model.Brokerage;
import com.sadoon.cbotback.brokerage.util.BrokerageDto;
import com.sadoon.cbotback.card.models.Card;
import com.sadoon.cbotback.card.models.CardApiRequest;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.user.models.User;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Mocks {

    public static User user() {
        return new User("mockUser", "mockPassword", new SimpleGrantedAuthority("USER"));
    }

    public static Balances balances(String currency, String amount) {
        Balances balances = new Balances(Map.of(currency, new BigDecimal(amount)));
        balances.unpackErrors(new String[]{});
        return balances;
    }

    public static Map<String, Card> cards() {
        Map<String, Card> cards = new LinkedHashMap<>();
        cards.put(
                "mockCard1",
                new Card("mockCard1", "mockPassword", balances("USD", "100"))
        );
        cards.put(
                "mockCard2",
                new Card("mockCard2", "mockPassword", balances("USD", "50"))
        );
        return cards;
    }

    public static Card card() {
        return new Card(
                "mockName",
                "mockPassword",
                balances("USD", "150")
        );
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

    public static <T> PublicRequestDto<T> publicRequestDto(T request, String type, String brokerageUrl) {
        PublicRequestDto<T> dto = new PublicRequestDto<>(request, type);
        dto.setBrokerage(brokerage(brokerageUrl));
        return dto;
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

    public static AssetPairs assetPairs() {
        AssetPairs assetPairs = new AssetPairs();
        assetPairs.setPairNames(Map.of("BTCUSD", new AssetPair()));
        assetPairs.unpackErrors(List.of("").toArray(String[]::new));
        return assetPairs;
    }

    public static Cookie cookie(String path, int maxAge){
        MockCookie cookie = new MockCookie("refresh_token", "mockToken");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setDomain("localhost");
        cookie.setPath(String.format("/api%s", path));
        return cookie;
    }

    public static RefreshResponse refreshResponse(String message){
        RefreshResponse response = new RefreshResponse("mockJwt", new Date());
        response.setHeaders(HttpHeaders.EMPTY);
        response.setMessage(message);
        return response;
    }

}