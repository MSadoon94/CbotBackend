package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.brokerage.BrokerageRequestConfig;
import com.sadoon.cbotback.brokerage.BrokerageRestService;
import com.sadoon.cbotback.brokerage.BrokerageUrlMapper;
import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

@RestController
public class AssetController {

    private BrokerageRestService service;

    @GetMapping("/asset-pair/{assets}/{brokerage}")
    public ResponseEntity<LinkedHashMap<String, Object>> getAssetPair(@PathVariable String assets, @PathVariable String brokerage) {
        service = new BrokerageRestService(
                new BrokerageRequestConfig().webClient(getBrokerageUrl(brokerage)));

        KrakenRequest request = new KrakenRequest();
        request.setEndpoint("0/public/AssetPairs?pair=" + assets);

        return getResponse(request);
    }

    private ResponseEntity<LinkedHashMap<String, Object>> getResponse(KrakenRequest request) {
        LinkedHashMap<String, Object> assetPairData = service.getAssetPair(request);
        ResponseEntity.BodyBuilder response;
        if (!assetPairData.containsKey("result")) {
            response = ResponseEntity.status(HttpStatus.NOT_FOUND);
        } else {
            response = ResponseEntity.ok();
        }

        return response.body(assetPairData);
    }

    private String getBrokerageUrl(String brokerage) {
        return new BrokerageUrlMapper().getBrokerageUrl(brokerage);
    }
}
