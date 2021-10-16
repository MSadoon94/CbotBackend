package com.sadoon.cbotback.brokerage.util;

import com.sadoon.cbotback.brokerage.BrokerageRepository;
import com.sadoon.cbotback.brokerage.BrokerageService;
import com.sadoon.cbotback.brokerage.WebClientService;
import org.springframework.stereotype.Component;

@Component
public class BrokerageApiModule {

    private BrokerageRepository brokerageRepo;

    private BrokerageService brokerageService;

    private WebClientService webClientService;

    public BrokerageApiModule(BrokerageRepository brokerageRepo) {
        this.brokerageService = new BrokerageService(brokerageRepo, new NonceCreator());
        this.webClientService = new WebClientService();
    }

    public BrokerageRepository getBrokerageRepo() {
        return brokerageRepo;
    }

    public void setBrokerageRepo(BrokerageRepository brokerageRepo) {
        this.brokerageRepo = brokerageRepo;
    }

    public BrokerageService getBrokerageService() {
        return brokerageService;
    }

    public void setBrokerageService(BrokerageService brokerageService) {
        this.brokerageService = brokerageService;
    }

    public WebClientService getWebClientService() {
        return webClientService;
    }

    public void setWebClientService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }
}
