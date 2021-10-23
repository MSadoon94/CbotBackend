package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.model.Brokerage;
import com.sadoon.cbotback.brokerage.util.BrokerageDto;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import com.sadoon.cbotback.card.models.CardApiRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SignatureCreatorTest {

    /* Keys used here are expired and were taken from the kraken api documentation sample code.
      They are only used as a constant to test the outcome of the signature creator against.*/
    private final String API_SIGN = "4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==";
    private final String ENDPOINT = "/0/private/AddOrder";
    private final String NONCE = "1616492376594";
    private final String SECRET_KEY = "kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXNsu3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg==";


    @Test
    void shouldGenerateSignatureThatMatchesApiSignConstant() {
        CardApiRequest request = new CardApiRequest("Account", SECRET_KEY);
        request.setBrokerage("kraken");
        BrokerageDto brokerageDTO = new BrokerageDto(request, "add-order");
        brokerageDTO.setBrokerage(mockBrokerage());
        brokerageDTO.setNonce(NONCE);
        addBodyValues(brokerageDTO);

        assertThat(new SignatureCreator()
                        .getSignature(brokerageDTO),
                is(API_SIGN));
    }

    private void addBodyValues(BrokerageDto brokerageDTO) {
        String[] keys = {"ordertype", "pair", "price", "type", "volume"};
        String[] values = {"limit", "XBTUSD", "37500", "buy", "1.25"};

        for (int i = 0; i < keys.length; i++) {
            brokerageDTO.addBodyValue(keys[i], values[i]);
        }
    }

    //This method is used instead of the static mock method in "Mocks" due to the specificity needed in this test.
    private Brokerage mockBrokerage() {
        Brokerage mockBrokerage = new Brokerage();
        mockBrokerage.setUrl("https://api.kraken.com");
        mockBrokerage.setName("kraken");
        mockBrokerage.setEndpoints(Map.of("add-order", "/0/private/AddOrder"));
        mockBrokerage.setSuccessKey("result");
        mockBrokerage.setMethods(Map.of("add-order", "POST"));
        return mockBrokerage;
    }


}
