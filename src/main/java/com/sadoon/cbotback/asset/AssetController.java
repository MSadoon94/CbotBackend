package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.api.PublicRequestDto;
import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssetController {

    private WebClientService webClientService;

    private BrokerageService brokerageService;

    public AssetController(BrokerageApiModule brokerageApiModule) {
        this.webClientService = brokerageApiModule.getWebClientService();
        this.brokerageService = brokerageApiModule.getBrokerageService();
    }

    @GetMapping("/asset-pair/{assets}/{brokerage}")
    public ResponseEntity<AssetPairs> getAssetPair(AssetPairRequest request) throws ExchangeRequestException {
        PublicRequestDto<AssetPairRequest> dto = brokerageService.createPublicDto(request, "asset-pair");
        dto.appendEndpoint(request.getAssets());

        return ResponseEntity.ok(
                webClientService.onResponse(
                        new ParameterizedTypeReference<AssetPairs>() {
                        }, dto)
        );
    }
}
