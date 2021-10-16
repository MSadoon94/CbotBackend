package com.sadoon.cbotback.brokerage.model;

import com.sadoon.cbotback.common.ApiRequest;

public interface BrokerageApiRequest extends ApiRequest {
    String getAccount();

    String getPassword();

}