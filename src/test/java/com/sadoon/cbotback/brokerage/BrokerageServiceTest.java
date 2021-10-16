package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.model.Brokerage;
import com.sadoon.cbotback.brokerage.util.BrokerageDto;
import com.sadoon.cbotback.brokerage.util.NonceCreator;
import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.common.PublicRequestDto;
import com.sadoon.cbotback.home.models.CardApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DataMongoTest
class BrokerageServiceTest {

    @Autowired
    private BrokerageRepository repo;

    @Mock
    private NonceCreator nonceCreator;

    private BrokerageService brokerageService;

    private Brokerage brokerage;

    private final String requestType = "balance";

    private CardApiRequest request = Mocks.cardRequest("mockKraken");

    @BeforeEach
    public void setup() {
        brokerageService = new BrokerageService(repo, nonceCreator);
        brokerage = repo.getBrokerageByName("mockKraken");

    }

    @Test
    void shouldSetFieldsForPublicDto() {
        PublicRequestDto<CardApiRequest> mockDto = new PublicRequestDto<>(request, requestType);
        mockDto.setBrokerage(brokerage);
        PublicRequestDto<CardApiRequest> publicRequestDto = brokerageService.createPublicDto(request, requestType);

        assertThat(publicRequestDto, samePropertyValuesAs(mockDto, "brokerage", "request"));
        assertThat(publicRequestDto, hasProperty("brokerage", samePropertyValuesAs(brokerage)));
        assertThat(publicRequestDto, hasProperty("request", samePropertyValuesAs(request)));
    }

    @Test
    void shouldSetFieldsForBrokerageDto() {
        given(nonceCreator.createNonce()).willReturn("mockNonce");
        BrokerageDto brokerageDTO = brokerageService.createBrokerageDto(request, requestType);
        assertThat(brokerageDTO,
                samePropertyValuesAs(
                        Mocks.brokerageDTO(requestType, brokerage.getUrl()), "brokerage", "request")
        );
        assertThat(brokerageDTO, hasProperty("brokerage", samePropertyValuesAs(brokerage)));
        assertThat(brokerageDTO, hasProperty("request", samePropertyValuesAs(request)));

    }
}
