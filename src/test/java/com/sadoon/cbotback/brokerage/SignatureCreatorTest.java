package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.brokerage.kraken.KrakenRequest;
import com.sadoon.cbotback.brokerage.util.SignatureCreator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest()
@ActiveProfiles("test")
public class SignatureCreatorTest {

    private final String API_SIGN = "4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==";
    private final String ENDPOINT = "/0/private/AddOrder";
    private final String NONCE = "1616492376594";
    private final String SECRET_KEY = "kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXNsu3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg==";


    @Test
    void shouldGenerateSignatureThatMatchesApiSignConstant() {
        KrakenRequest request = new KrakenRequest("Account", SECRET_KEY);
        request.setNonce(NONCE);
        request.setEndpoint(ENDPOINT);
        addBodyValues(request);

        assertThat(new SignatureCreator()
                        .getSignature(request),
                is(API_SIGN));
    }

    private void addBodyValues(KrakenRequest request) {
        String[] keys = {"ordertype", "pair", "price", "type", "volume"};
        String[] values = {"limit", "XBTUSD", "37500", "buy", "1.25"};

        for (int i = 0; i < keys.length; i++) {
            request.addBodyValue(keys[i], values[i]);
        }
    }


}
