package com.sadoon.cbotback.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadoon.cbotback.asset.AssetPair;
import com.sadoon.cbotback.asset.AssetPairMessage;
import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.exchange.kraken.KrakenTickerMessage;
import com.sadoon.cbotback.exchange.meta.ExchangeName;
import com.sadoon.cbotback.exchange.meta.TradeStatus;
import com.sadoon.cbotback.exchange.model.*;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.refresh.models.RefreshToken;
import com.sadoon.cbotback.status.CbotStatus;
import com.sadoon.cbotback.strategy.Strategy;
import com.sadoon.cbotback.strategy.StrategyType;
import com.sadoon.cbotback.trade.Trade;
import com.sadoon.cbotback.user.models.LoginRequest;
import com.sadoon.cbotback.user.models.LoginResponse;
import com.sadoon.cbotback.user.models.SignUpRequest;
import com.sadoon.cbotback.user.models.User;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;

import javax.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Mocks {

    public static ObjectMapper mapper = new ObjectMapper();

    public static User user() {
        return new User("mockUser", "mockPassword", new SimpleGrantedAuthority("USER"));
    }

    public static Balances balances(String currency, String amount) {
        Balances balances = new Balances(Map.of(currency, new BigDecimal(amount)));
        balances.setErrors(new String[]{});
        return balances;
    }

    public static Map<String, Exchange> cards() {
        Map<String, Exchange> cards = new LinkedHashMap<>();
        cards.put(
                "exchange1",
                new Exchange("exchange1", balances("USD", "100"))
        );
        cards.put(
                "exchange2",
                new Exchange("exchange2", balances("USD", "50"))
        );
        return cards;
    }

    public static Authentication auth(User mockUser) {
        return new UsernamePasswordAuthenticationToken(mockUser, "mockPassword", mockUser.getAuthorities());
    }

    public static AssetPairMessage assetPairMessage() {
        AssetPairMessage request = new AssetPairMessage();
        request.setAssets("BTCUSD");
        request.setExchange("kraken");
        return request;
    }

    public static AssetPairs assetPairs() {
        AssetPairs assetPairs = new AssetPairs();
        assetPairs.setPairs(Map.of("BTC/USD", assetPair()));
        assetPairs.setErrors(new String[]{});
        return assetPairs;
    }

    public static AssetPair assetPair() {
        AssetPair pair = new AssetPair();
        pair.setBase("BTC");
        pair.setQuote("USD");
        pair.setAltName("XBTUSD");
        pair.setWsName("BTC/USD");
        String[] fees = new String[]{"0", "0.26"};
        pair.setFeeSchedule(List.of(fees, fees));
        pair.setMakerTakerFees(List.of(fees, fees));
        return pair;
    }

    public static Cookie refreshCookie(String path, int maxAge) {
        MockCookie cookie = new MockCookie("refresh_token", "mockToken");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setDomain("localhost");
        cookie.setPath(String.format("/api%s", path));
        return cookie;
    }

    public static RefreshResponse refreshResponse(Date date) {
        RefreshResponse response = new RefreshResponse("mockJwt", date);
        return response;
    }

    public static RefreshToken refreshToken(int expirationMs) {
        return new RefreshToken(
                UUID.randomUUID().toString(),
                Instant.now().plusMillis(expirationMs));
    }

    public static ResponseCookie refreshCookie(RefreshToken refreshToken, String path) {
        return ResponseCookie
                .from("refresh_token", refreshToken.getToken())
                .httpOnly(true)
                .domain("localhost")
                .path(String.format("%s", path))
                .maxAge(Duration.between(Instant.now(), refreshToken.getExpiryDate()))
                .build();
    }

    public static ResponseCookie jwtCookie(String jwt, Date expiration) {
        return ResponseCookie
                .from("jwt", jwt)
                .httpOnly(true)
                .domain("localhost")
                .path("/")
                .build();
    }

    public static ResponseCookie nullCookie(String name, String path) {
        return ResponseCookie
                .from(name, null)
                .httpOnly(true)
                .domain("localhost")
                .path(String.format("%s", path))
                .maxAge(-1)
                .build();
    }

    public static LoginResponse loginResponse(Date date) {
        return new LoginResponse("username", date);
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest("mockUser", "password", "mockId");
    }

    public static HttpHeaders refreshHeaders(RefreshToken token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", refreshCookie(token, "/refresh-jwt").toString());
        headers.add("Set-Cookie", refreshCookie(token, "/log-out").toString());
        return headers;
    }

    public static SignUpRequest signUpRequest() {
        return new SignUpRequest("mockUser", "password", "USER");
    }

    public static Strategy strategy() {
        Strategy strategy = new Strategy();
        strategy.setType(StrategyType.LONG.name());
        strategy.setExchange(ExchangeName.KRAKEN.name());
        strategy.setName("mockStrategy");
        strategy.setBase("BTC");
        strategy.setQuote("USD");
        strategy.setStopLoss("1000");
        strategy.setMaxPosition("5000");
        strategy.setTargetProfit("3000");
        strategy.setMaxLoss("10000");
        strategy.setEntry("10");
        return strategy;
    }

    public static CbotStatus cbotStatus() {
        return new CbotStatus(true, List.of(strategy().getName()));
    }

    public static Flux<WebSocketMessage> mockMessageFlux(List<String> payloads) {
        DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
        return Flux.fromIterable(payloads)
                .map(payload -> new WebSocketMessage(
                                WebSocketMessage.Type.TEXT,
                                dataBufferFactory.wrap(payload.getBytes())
                        )
                );

    }

    public static KrakenTickerMessage krakenTickerMessage(String[] mockValues) {
        KrakenTickerMessage message = new KrakenTickerMessage();
        message.setTimestamp();
        message.setPair("BTC/USD");
        message.setAsk(mockValues);
        message.setBid(mockValues);
        message.setOpen(mockValues);
        message.setClose(mockValues);
        message.setHigh(mockValues);
        message.setLow(mockValues);
        message.setVolume(mockValues);
        return message;
    }

    public static TradeVolume tradeVolume(ObjectMapper mapper, String[] errors) throws JsonProcessingException {
        TradeVolume volume = new TradeVolume();
        volume.setErrors(errors);
        volume.setProperties("result", Map.of(
                "volume", "mockVolume",
                "fees", Map.of(fees().getPair(), mapper.writeValueAsString(fees()))
        ));
        return volume;
    }

    public static Fees fees() {
        Fees fees = new Fees();
        fees.setFee("0.1000");
        fees.setMinFee("0.1000");
        fees.setMaxFee("0.2600");
        fees.setNextFee("null");
        fees.setNextVolume("null");
        fees.setTierVolume("10000000.0000");
        return fees.setPair("BTC/USD");
    }

    public static Trade trade(TradeStatus status, BigDecimal currentPrice, BigDecimal targetPrice) {
        return new Trade()
                .setStatus(status)
                .setExchange(ExchangeName.KRAKEN)
                .setPair(String.join("/", Mocks.assetPair().getBase(), Mocks.assetPair().getQuote()))
                .setType(StrategyType.LONG)
                .setFees(Mocks.fees())
                .setCurrentPrice(currentPrice)
                .setTargetPrice(targetPrice)
                .addPairNames(List.of(Mocks.assetPair().getAltName(), Mocks.assetPair().getWsName()))
                .setEntryPercentage(new BigDecimal(strategy().getEntry()));
    }

    public static Candle candle(){
        return new Candle(1l, "5", "10", "1", "9");
    }
}