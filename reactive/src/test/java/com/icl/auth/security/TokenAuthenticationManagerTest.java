package com.icl.auth.security;

import com.icl.auth.exception.TokenNotFoundException;
import com.icl.auth.model.ExpiredToken;
import com.icl.auth.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class TokenAuthenticationManagerTest {
    @Autowired
    private TokenAuthenticationManager manager;

    @MockBean
    private TokenService tokenService;

    @Test
    void authorizeShouldReturnNotExpiredToken() {
        String tokenIdInDatabase = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJhY2Nlc3MgdG8gc2VjdXJlZCB6b25lIiwiZXhwIjoxNTk0ODAxNTgwLCJpYXQiOjE1OTQ3OTk3ODAsInVzZXIiOiJxIn0" +
                ".T_mHWYOOvjdoDvh6ouv-QbnVeTNqhHnBdL2t51cTnh8";
        String id = "expiredTokenId";
        ExpiredToken expiredToken = new ExpiredToken(id);
        when(tokenService.findExpiredTokenById(anyString())).thenReturn(Mono.error(TokenNotFoundException::new));

        StepVerifier.create(manager.authorize(tokenIdInDatabase))
                .expectNext(tokenIdInDatabase);
    }

    @Test
    void authorizeShouldReturnMonoTokenExpiredException() {
        String id = "expiredTokenId";
        ExpiredToken expiredToken = new ExpiredToken(id);
        when(tokenService.findExpiredTokenById(anyString())).thenReturn(Mono.just(expiredToken));

        StepVerifier.create(manager.authorize(id))
                .expectNext(expiredToken.toString());
    }
}