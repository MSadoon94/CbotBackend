package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.api.ApiRequest;
import com.sadoon.cbotback.api.PublicRequestDto;
import com.sadoon.cbotback.brokerage.model.Brokerage;
import com.sadoon.cbotback.brokerage.model.BrokerageApiRequest;
import com.sadoon.cbotback.brokerage.util.BrokerageDto;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.exceptions.not_found.BrokerageNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrokerageService {

    private BrokerageRepository repo;

    private NonceCreator nonceCreator;

    public BrokerageService(BrokerageRepository repo, NonceCreator nonceCreator) {
        this.repo = repo;
        this.nonceCreator = nonceCreator;
    }

    public <T extends ApiRequest> PublicRequestDto<T> createPublicDto(T request, String type) {
        Brokerage brokerage = repo.getBrokerageByName(request.getBrokerage());

        PublicRequestDto<T> publicDto = new PublicRequestDto<>(request, type);
        publicDto.setBrokerage(brokerage);

        return publicDto;
    }

    public BrokerageDto createBrokerageDto(BrokerageApiRequest request, String type)
            throws BrokerageNotFoundException {

        Brokerage brokerage =
                Optional.ofNullable(repo.getBrokerageByName(request.getBrokerage()))
                        .orElseThrow(() -> new BrokerageNotFoundException(request.getBrokerage()));

        BrokerageDto brokerageDTO = new BrokerageDto(request, type);
        brokerageDTO.setBrokerage(brokerage);
        brokerageDTO.setNonce(nonceCreator.createNonce());

        return brokerageDTO;
    }

}
