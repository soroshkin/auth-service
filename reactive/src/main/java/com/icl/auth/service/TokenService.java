package com.icl.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.icl.auth.model.ExpiredToken;
import com.icl.auth.model.User;
import com.icl.auth.repository.ReactiveExpiredTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {
    private ReactiveExpiredTokenRepository expiredTokenRepository;

    @Autowired
    public TokenService(ReactiveExpiredTokenRepository expiredTokenRepository) {
        this.expiredTokenRepository = expiredTokenRepository;
    }

    public Mono<ExpiredToken> createExpiredToken(String token) {
        return expiredTokenRepository.create(new ExpiredToken(token));
    }

    public Mono<ExpiredToken> findExpiredTokenById(String id) {
        return expiredTokenRepository.findById(id);
    }

    /**
     * Method creates token
     * @param user for whom token will be created
     * @return created token
     */
    public String createToken(User user) {
        JWTCreator.Builder builder = JWT.create();

        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HMAC256");

        Date issuedAt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(issuedAt);
        c.add(Calendar.MINUTE, 1000);
        Date expiredDate = c.getTime();

        return builder.withHeader(header)
                .withIssuedAt(issuedAt)
                .withSubject("access to secured zone")
                .withExpiresAt(expiredDate)
                .withClaim("user", user.getLogin())
                .sign(Algorithm.HMAC256("aM1?@30Oi_"));
    }
}
