package com.sadoon.cbotback.api;

import com.sadoon.cbotback.brokerage.model.Brokerage;
import org.springframework.http.HttpMethod;

public class PublicRequestDto<T> {
    private final T request;
    private String appendedEndpoint;
    private Brokerage brokerage;
    private final String requestType;

    public PublicRequestDto(T request, String requestType) {
        this.request = request;
        this.requestType = requestType;
    }

    public HttpMethod getMethod() {
        return brokerage.getMethod(requestType);
    }

    public String getEndpoint() {
        return appendedEndpoint == null ? brokerage.getEndpoint(requestType) : appendedEndpoint;
    }

    public void appendEndpoint(String addition) {
        appendedEndpoint = String.format("%1$s%2$s", brokerage.getEndpoint(requestType), addition);
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

    public T getRequest() {
        return request;
    }
}
