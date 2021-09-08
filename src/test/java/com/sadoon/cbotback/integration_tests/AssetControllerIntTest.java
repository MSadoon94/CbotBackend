package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.asset.AssetController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class AssetControllerIntTest {

    private AssetController controller;
    private final String VALID_ASSETS = "BTCUSD";
    private final String INVALID_ASSETS = "BTCUD";
    private final String BROKERAGE = "kraken";

    @BeforeEach
    void setUp() {
        controller = new AssetController();
    }

    @Test
    void shouldReturnHttpOkStatusForValidAssetPair() {
        assertThat(controller.getAssetPair(VALID_ASSETS, BROKERAGE).getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void shouldReturnResponseWithAssetPairInformationForValidAssetPair() {
        assertThat(controller.getAssetPair(VALID_ASSETS, BROKERAGE).getBody(), hasKey("result"));
    }

    @Test
    void shouldReturnHttpNotFoundStatusForInvalidAssetPair() {
        assertThat(controller.getAssetPair(INVALID_ASSETS, BROKERAGE).getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldReturnErrorResponseForInvalidAssetPair() {
        ArrayList<String> errors =
                (ArrayList<String>) controller.getAssetPair(INVALID_ASSETS, BROKERAGE).getBody().get("error");

        assertThat(errors, contains("EQuery:Unknown asset pair"));
    }


}
