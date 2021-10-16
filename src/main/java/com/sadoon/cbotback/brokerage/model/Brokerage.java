package com.sadoon.cbotback.brokerage.model;

import org.springframework.http.HttpMethod;

import java.util.Map;

public class Brokerage {
    private String name;
    private String url;
    private String successKey;
    private Map<String, String> endpoints;
    private Map<String, String> methods;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSuccessKey(String successKey) {
        this.successKey = successKey;
    }

    public String getSuccessKey() {
        return successKey;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }

    public String getEndpoint(String type) {
        return endpoints.get(type);
    }

    public Map<String, String> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, String> methods) {
        this.methods = methods;
    }

    public HttpMethod getMethod(String type) {
        return HttpMethod.valueOf(methods.get(type));
    }
}
