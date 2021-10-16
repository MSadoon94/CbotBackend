package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import com.sadoon.cbotback.brokerage.util.BrokerageApiModule;
import com.sadoon.cbotback.common.PublicRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;

@RestController
public class AssetController {

    private WebClientService webClientService;

    private BrokerageService brokerageService;

    public AssetController(BrokerageApiModule brokerageApiModule) {
        this.webClientService = brokerageApiModule.getWebClientService();
        this.brokerageService = brokerageApiModule.getBrokerageService();
    }

    @GetMapping("/asset-pair/{assets}/{brokerage}")
    public ResponseEntity<LinkedHashMap<String, List<String>>> getAssetPair(AssetPairRequest request) {
        PublicRequestDto<AssetPairRequest> dto = brokerageService.createPublicDto(request, "asset-pair");
        dto.appendEndpoint(request.getAssets());

        return getResponse(dto);
    }


    private ResponseEntity<LinkedHashMap<String, List<String>>> getResponse(PublicRequestDto<AssetPairRequest> dto) {
        LinkedHashMap<String, List<String>> assetPairData = webClientService.onResponse(dto);
        ResponseEntity.BodyBuilder response;
        if (!assetPairData.containsKey("result")) {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND);
        } else {
            response = ResponseEntity.ok();
        }

        return response.body(assetPairData);
    }
}
