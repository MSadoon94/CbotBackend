package com.sadoon.cbotback.integration_tests;

import com.sadoon.cbotback.api.PublicRequestDto;
import com.sadoon.cbotback.asset.AssetController;
import com.sadoon.cbotback.asset.AssetPairRequest;
import com.sadoon.cbotback.asset.AssetPairs;
import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.KrakenRequestException;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@JsonTest
class AssetControllerTest {

    @Mock
    private WebClientService webClientService;
    @Mock
    private BrokerageService brokerageService;
    @Mock
    private BrokerageApiModule brokerageApiModule;

    @InjectMocks
    private AssetController controller;

    private AssetPairRequest mockRequest = Mocks.assetPairRequest();
    private PublicRequestDto<AssetPairRequest> dto
            = Mocks.publicRequestDto(mockRequest, "asset-pair", "krakenUrl");
    private AssetPairs mockAssetPairs = Mocks.assetPairs();

    private User mockUser = Mocks.user();
    private final Authentication auth = Mocks.auth(mockUser);

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    void shouldReturnResponseWithAssetPairInformationForValidAssetPair() throws Exception {
        given(brokerageService.createPublicDto(any(AssetPairRequest.class), any())).willReturn(dto);
        given(webClientService.onResponse(any(), any())).willReturn(mockAssetPairs);

        getAssetPair()
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.pairNames", hasKey(mockRequest.getAssets())));
    }

    @Test
    void shouldReturnErrorResponseForInvalidAssetPair() throws Exception {
        KrakenRequestException exception = new KrakenRequestException(Arrays.toString(new String[]{"InvalidAssets"}));

        given(brokerageService.createPublicDto(any(AssetPairRequest.class), any())).willReturn(dto);
        given(webClientService.onResponse(any(), any())).willThrow(exception);

        getAssetPair()
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));
    }

    private ResultActions getAssetPair() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders.get("/asset-pair/BTCUSD/kraken")
                        .principal(auth)
        );
    }
}
