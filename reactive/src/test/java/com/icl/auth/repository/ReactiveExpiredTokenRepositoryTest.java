package com.icl.auth.repository;

import com.icl.auth.exception.TokenNotFoundException;
import com.icl.auth.model.ExpiredToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.test.StepVerifier;

import java.util.Objects;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReactiveExpiredTokenRepositoryTest {
    private String tokenIdInDatabase = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
            ".eyJzdWIiOiJhY2Nlc3MgdG8gc2VjdXJlZCB6b25lIiwiZXhwIjoxNTk0ODAxNTgwLCJpYXQiOjE1OTQ3OTk3ODAsInVzZXIiOiJxIn0" +
            ".T_mHWYOOvjdoDvh6ouv-QbnVeTNqhHnBdL2t51cTnh8";

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    void findByIdShouldReturnMono() {
        StepVerifier.create(tokenRepository.findById(tokenIdInDatabase))
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
    }

    @Test
    void findByIdShouldThrowException() {
        StepVerifier.create(tokenRepository.findById("notExistentTokenId"))
                .expectError(TokenNotFoundException.class);
    }

    @Test
    void createTokenShouldCreateToken() {
        ExpiredToken expiredToken = new ExpiredToken("expiredTokenId");

        StepVerifier.create(tokenRepository.create(expiredToken))
                .expectNextMatches(token -> token.equals(expiredToken))
                .verifyComplete();
    }

    @Test
    void deleteTokenShouldDeleteToken() {
        tokenRepository.delete(new ExpiredToken(tokenIdInDatabase)).subscribe();

        StepVerifier.create(tokenRepository.findById(tokenIdInDatabase))
                .verifyError(TokenNotFoundException.class);
    }
}
