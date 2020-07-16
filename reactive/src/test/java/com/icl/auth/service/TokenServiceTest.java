package com.icl.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.icl.auth.model.ExpiredToken;
import com.icl.auth.model.User;
import com.icl.auth.repository.ReactiveExpiredTokenRepository;
import com.icl.auth.security.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @MockBean
    private ReactiveExpiredTokenRepository repository;

    @Test
    public void getTokenShouldReturnToken() {
        User user = new User("superUser", "rtw", LocalDate.MIN, Role.USER);

        DecodedJWT decodedJWT = JWT.decode(tokenService.createToken(user));
        Assertions.assertThat(decodedJWT.getType()).isEqualTo("JWT");
        Assertions.assertThat(decodedJWT.getClaim("user").asString()).isEqualTo(user.getLogin());
    }

    @Test
    void createExpiredTokenShouldReturnExpiredToken() {
        String id = "expiredToken";
        ExpiredToken expiredToken = new ExpiredToken(id);
        when(repository.create(any(ExpiredToken.class))).thenReturn(Mono.just(expiredToken));

        StepVerifier.create(tokenService.createExpiredToken(id))
                .expectNext(expiredToken);
    }
}
