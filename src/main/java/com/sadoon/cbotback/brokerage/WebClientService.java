package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.api.KrakenResponse;
import com.sadoon.cbotback.api.PublicRequestDto;
import com.sadoon.cbotback.brokerage.util.BrokerageRequestConfig;
import com.sadoon.cbotback.exceptions.KrakenRequestException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class WebClientService {

    public <T extends KrakenResponse, V extends PublicRequestDto<?>> T onResponse(
            ParameterizedTypeReference<T> type, V dto) throws KrakenRequestException {
        T response = new BrokerageRequestConfig().webClient(dto.getUrl())
                .method(dto.getMethod())
                .uri(dto.getEndpoint())
                .attribute("request", dto)
                .retrieve()
                .bodyToMono(type)
                .log()
                .block();
        response.checkErrors(response.getErrors());
        return response;
    }

}
