package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.asset.AssetController;
import com.sadoon.cbotback.asset.AssetPairRequest;
import com.sadoon.cbotback.brokerage.BrokerageRepository;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.common.Mocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class AssetControllerIntTest {

    @Autowired
    private BrokerageRepository brokerageRepo;
    private AssetController controller;
    private AssetPairRequest request;

    @BeforeEach
    void setUp() {
        request = Mocks.assetPairRequest();
        controller = new AssetController(new BrokerageApiModule(brokerageRepo));
    }

    @Test
    void shouldReturnHttpOkStatusForValidAssetPair() {

        assertThat(controller.getAssetPair(request).getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void shouldReturnResponseWithAssetPairInformationForValidAssetPair() {
        assertThat(controller.getAssetPair(request).getBody(), hasKey("result"));
    }

    @Test
    void shouldReturnHttpNotFoundStatusForInvalidAssetPair() {
        request.setAssets("BTCUD");
        assertThat(controller.getAssetPair(request).getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldReturnErrorResponseForInvalidAssetPair() {
        request.setAssets("BTCUD");
        List<String> errors = controller.getAssetPair(request).getBody().get("error");

        assertThat(errors, contains("EQuery:Unknown asset pair"));
    }


}
