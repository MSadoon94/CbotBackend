package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.util.BrokerageRequestConfig;
import com.sadoon.cbotback.common.PublicRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class WebClientService {

    public <T, V extends PublicRequestDto<?>> T onResponse(V dto) {
        return new BrokerageRequestConfig().webClient(dto.getUrl())
                .method(dto.getMethod())
                .uri(dto.getEndpoint())
                .attribute("request", dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<T>() {
                })
                .block();
    }

}
