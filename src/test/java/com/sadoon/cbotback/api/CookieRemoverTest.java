package com.sadoon.cbotback.api;

import com.sadoon.cbotback.tools.Mocks;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CookieRemoverTest {

    private HttpHeaders mockHeaders = new HttpHeaders();

    @Test
    void shouldReturnNullCookieHeaders(){
        setMockHeaders(
                Mocks.nullCookie("refresh_token", "/refresh-jwt"),
                Mocks.nullCookie("refresh_token", "/log-out"),
                Mocks.nullCookie("jwt", "/")
        );

        assertThat(CookieRemover.getNullHeaders(), is(mockHeaders));
    }

    private void setMockHeaders(ResponseCookie... cookies){
        for(ResponseCookie cookie : cookies){
            mockHeaders.add("Set-Cookie", cookie.toString());
        }
    }
}