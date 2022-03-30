package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.exceptions.exchange.ExchangeRequestException;
import com.sadoon.cbotback.exchange.structure.Exchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AssetController {

    private Map<String, Exchange> exchanges;

    public AssetController(Map<String, Exchange> exchanges){
        this.exchanges = exchanges;
    }

    @GetMapping("/asset-pair/{base}/{quote}/{exchange}")
    public ResponseEntity<AssetPairs> getAssetPair(@PathVariable String base, @PathVariable String quote, @PathVariable String exchange) throws ExchangeRequestException {
        AssetPairs pairs = exchanges.get(exchange.toLowerCase()).getWebClient()
                        .assetPairs(String.format("%1s/%2s", base, quote))
                        .block();
        pairs.checkErrors(pairs.getErrors());
        return ResponseEntity.ok(pairs);
    }
}
