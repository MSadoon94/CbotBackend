package com.sadoon.cbotback.refresh;

import com.sadoon.cbotback.common.Mocks;
import com.sadoon.cbotback.exceptions.GlobalExceptionHandler;
import com.sadoon.cbotback.exceptions.RefreshExpiredException;
import com.sadoon.cbotback.exceptions.RefreshTokenNotFoundException;
import com.sadoon.cbotback.refresh.models.RefreshResponse;
import com.sadoon.cbotback.user.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RefreshControllerTest {

    private MockMvc mvc;

    private User mockUser = Mocks.user();

    private final Authentication auth = Mocks.auth(mockUser);

    private RefreshResponse mockResponse = Mocks.refreshResponse(new Date());

    @Mock
    private RefreshService refreshService;

    @InjectMocks
    private RefreshController refreshController;

    @BeforeEach
    public void setup(){
        mvc = MockMvcBuilders.standaloneSetup(refreshController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnRefreshResponseOnRefreshJwtSuccess() throws Exception {
        given(refreshService.refresh(any(), any())).willReturn(mockResponse);
        given(refreshService.getResponseCookie(any(), any(), any()))
                .willReturn(Mocks.responseCookie(Mocks.refreshToken(10000), "/refresh-jwt"));

        refreshJwt()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt", is(mockResponse.getJwt())));
    }

    @Test
    void shouldThrowNotFoundWhenRefreshTokenCannotBeFoundOnRefreshJwt() throws Exception {
        given(refreshService.refresh(any(), any())).willThrow(new RefreshTokenNotFoundException("mockToken"));
        refreshJwt()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Refresh Token: mockToken was not found.")));
    }

    @Test
    void shouldThrowUnauthorizedStatusWhenRefreshTokenIsExpiredOnRefreshJwt() throws Exception{
        given(refreshService.refresh(any(), any())).willThrow(new RefreshExpiredException());
        refreshJwt()
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message",
                        is("User is not logged in due to: Refresh token has expired. Please login and try again.")));

    }

    @Test
    void shouldReturnNoContentOnLogout() throws Exception {
        logout()
                .andExpect(status().isNoContent());
    }

    private ResultActions refreshJwt() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .post("/refresh-jwt")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(Mocks.refreshCookie("/refresh-jwt", 100))
                .principal(auth));
    }

    private ResultActions logout() throws Exception {
        return mvc.perform(MockMvcRequestBuilders
                .delete("/log-out")
                .cookie(Mocks.refreshCookie("/log-out", 100))
        );

    }
}
