package com.sadoon.cbotback.asset;

import com.sadoon.cbotback.api.ApiRequest;

public class AssetPairRequest implements ApiRequest {
    private String assets;
    private String brokerage;

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public String getBrokerage() {
        return brokerage;
    }

    public void setBrokerage(String brokerage) {
        this.brokerage = brokerage;
    }
}