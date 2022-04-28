package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.api.ApiRequest;

public class AssetPairMessage implements ApiRequest {
    private String assets;
    private String exchange;

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}