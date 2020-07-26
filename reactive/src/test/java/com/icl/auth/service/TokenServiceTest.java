package com.icl.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.icl.auth.config.SecurityProperties;
import com.icl.auth.model.BlockedToken;
import com.icl.auth.model.User;
import com.icl.auth.repository.ReactiveBlockedTokenRepository;
import com.icl.auth.security.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SecurityProperties securityProperties;

    @MockBean
    private ReactiveBlockedTokenRepository repository;

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
        BlockedToken blockedToken = new BlockedToken(id);
        when(repository.create(any(BlockedToken.class))).thenReturn(Mono.just(blockedToken));

        StepVerifier.create(tokenService.createBlockedToken(id))
                .expectNext(blockedToken);
    }

    @Test
    void deleteExpiredTokensShouldDelete() {
        BlockedToken expiredBlockedTokenFirst = new BlockedToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJhY2Nlc3MgdG8gc2VjdXJlZCB6b25lIiwiZXhwIjoxNTk0ODAxNTgwLCJpYXQiOjE1OTQ3OTk3ODAsInVzZXIiOiJxIn0" +
                ".T_mHWYOOvjdoDvh6ouv-QbnVeTNqhHnBdL2t51cTnh8");
        BlockedToken expiredBlockedTokenSecond = new BlockedToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJhY2Nlc3MgdG8gc2VjdXJlZCB6b25lIiwiZXhwIjoxNTk0ODAxNTgwLCJpYXQiOjE1OTQ3OTk3ODAsInVzZXIiOiJxIn0" +
                ".T_mHWYOOvjdoDvh6ouv-QbnVeTNqhHnBdL2t51cTnh9");
        BlockedToken notExpiredBlockedToken = new BlockedToken(createToken());

        when(repository.delete(expiredBlockedTokenFirst)).thenReturn(Mono.just(expiredBlockedTokenFirst));
        when(repository.delete(expiredBlockedTokenSecond)).thenReturn(Mono.just(expiredBlockedTokenSecond));
        when(repository.delete(notExpiredBlockedToken)).thenReturn(Mono.just(notExpiredBlockedToken));

        when(repository.findAll()).thenReturn(Flux.just(expiredBlockedTokenFirst, expiredBlockedTokenSecond, notExpiredBlockedToken));

        StepVerifier.create(tokenService.deleteExpiredTokens())
                .expectNext(expiredBlockedTokenFirst)
                .expectNext(expiredBlockedTokenSecond)
                .verifyComplete();
    }

    private String createToken() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HMAC256");

        Date issuedAt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(issuedAt);
        c.add(Calendar.MINUTE, 30);
        Date expiredDate = c.getTime();

        return JWT.create()
                .withHeader(header)
                .withIssuedAt(issuedAt)
                .withSubject("access to secured zone")
                .withExpiresAt(expiredDate)
                .withClaim("user", "user")
                .sign(Algorithm.HMAC256(securityProperties.getSalt()));
    }
}
