package com.sadoon.cbotback.brokerage;

import com.sadoon.cbotback.security.util.SignatureCreator;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SignatureCreatorTest {

    /* Keys used here are expired and were taken from the kraken api documentation sample code.
      They are only used as a constant to test the outcome of the signature creator against.*/
    private final String API_SIGN = "4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==";
    private final String ENDPOINT = "/0/private/AddOrder";
    private final String NONCE = "1616492376594";
    private final String SECRET_KEY = "kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXN==su3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg==";
    private MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();


    @Test
    void shouldGenerateSignatureThatMatchesApiSignConstant() {
        setBodyValues();

        assertThat(new SignatureCreator()
                .signature(
                        NONCE.concat(formattedBody()),
                        SECRET_KEY,
                        ENDPOINT
                ), is(API_SIGN));
    }

    private void setBodyValues() {
        String[] keys = {"ordertype", "pair", "price", "type", "volume"};
        String[] values = {"limit", "XBTUSD", "37500", "buy", "1.25"};
        bodyValues.set("nonce", NONCE);
        for (int i = 0; i < keys.length; i++) {
            bodyValues.add(keys[i], values[i]);
        }
    }

    private String formattedBody() {
        String bodyValuesAsText = bodyValues.toString();
        bodyValuesAsText = bodyValuesAsText.replaceAll("[{}\\[\\],]", "").trim();
        return bodyValuesAsText.replaceAll("[\\s]", "&");
    }


}
