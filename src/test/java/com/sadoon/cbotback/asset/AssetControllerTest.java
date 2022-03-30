package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.api.PublicRequestDto;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.structure.Exchange;
import com.sadoon.cbotback.exchange.meta.ExchangeType;
import com.sadoon.cbotback.exchange.kraken.KrakenWebClient;
import com.sadoon.cbotback.tools.Mocks;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @Mock
    private KrakenWebClient webClient;

    private Exchange exchange;

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
        exchange = new Exchange()
                .setWebClient(webClient);
        controller = new AssetController(Map.of("kraken", exchange));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    void shouldReturnResponseWithAssetPairsForValidAssetPair() throws Exception {
        given(webClient.assetPairs(any())).willReturn(Mono.just(mockAssetPairs));


        getAssetPair()
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.pairs", hasKey("BTC/USD")));
    }

    @Test
    void shouldReturnErrorResponseForInvalidAssetPair() throws Exception {
        ExchangeRequestException exception =
                new ExchangeRequestException(ExchangeType.KRAKEN, Arrays.toString(new String[]{"InvalidAssets"}));

        mockAssetPairs.setErrors(new String[]{"InvalidAssets"});
        given(webClient.assetPairs(any())).willReturn(Mono.just(mockAssetPairs));

        getAssetPair()
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is(exception.getMessage())));
    }

    private ResultActions getAssetPair() throws Exception {
        return mvc.perform(
                MockMvcRequestBuilders.get("/asset-pair/BTC/USD/kraken")
                        .principal(auth)
        );
    }
}
