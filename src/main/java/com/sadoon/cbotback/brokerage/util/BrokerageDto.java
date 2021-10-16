package com.sadoon.cbotback.brokerage.util;

import com.sadoon.cbotback.brokerage.model.Brokerage;
import com.sadoon.cbotback.brokerage.model.BrokerageApiRequest;
import com.sadoon.cbotback.common.PublicRequestDto;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class BrokerageDto extends PublicRequestDto<BrokerageApiRequest> {
    private BrokerageApiRequest request;
    private String nonce;
    private Brokerage brokerage;
    private final String requestType;
    private MultiValueMap<String, String> bodyValues;

    public BrokerageDto(BrokerageApiRequest request, String requestType) {
        super(request, requestType);
        bodyValues = new LinkedMultiValueMap<>();
        this.request = request;
        this.requestType = requestType;
    }

    public HttpMethod getMethod() {
        return brokerage.getMethod(requestType);
    }

    public String getEndpoint() {
        return brokerage.getEndpoint(requestType);
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
        bodyValues.set("nonce", nonce);
    }

    public MultiValueMap<String, String> getBodyValues() {
        return bodyValues;
    }

    public void addBodyValue(String key, String value) {
        bodyValues.add(key, value);
    }

    public String getAccount() {
        return request.getAccount();
    }

    public String getPassword() {
        return request.getPassword();
    }

    public Brokerage getBrokerage() {
        return brokerage;
    }

    public void setBrokerage(Brokerage brokerage) {
        this.brokerage = brokerage;
    }

    public String getUrl() {
        return brokerage.getUrl();
    }
}
