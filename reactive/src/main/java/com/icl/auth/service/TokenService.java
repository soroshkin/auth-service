package com.icl.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.icl.auth.config.SecurityProperties;
import com.icl.auth.model.BlockedToken;
import com.icl.auth.model.User;
import com.icl.auth.repository.ReactiveBlockedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableScheduling
public class TokenService {
    private ReactiveBlockedTokenRepository blockedTokenRepository;
    private SecurityProperties securityProperties;

    @Autowired
    public TokenService(ReactiveBlockedTokenRepository blockedTokenRepository, SecurityProperties securityProperties) {
        this.blockedTokenRepository = blockedTokenRepository;
        this.securityProperties = securityProperties;
    }

    public Mono<BlockedToken> createBlockedToken(String token) {
        return blockedTokenRepository.create(new BlockedToken(token));
    }

    public Mono<BlockedToken> findBlockedTokenById(String id) {
        return blockedTokenRepository.findById(id);
    }

    /**
     * Method creates token
     *
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
        c.add(Calendar.MINUTE, 30);
        Date expiredDate = c.getTime();

        return builder.withHeader(header)
                .withIssuedAt(issuedAt)
                .withSubject("access to secured zone")
                .withExpiresAt(expiredDate)
                .withClaim("user", user.getLogin())
                .sign(Algorithm.HMAC256(securityProperties.getSalt()));
    }

    /**
     * Deletes all expired tokens from BLOCKED_TOKEN table every midnight
     *
     * @return {@link Flux} of BlockedToken, if token was deleted or Mono.empty if not
     */
    @Scheduled(cron = "* * 0 * * ?")
    public Flux<BlockedToken> deleteExpiredTokens() {
        return blockedTokenRepository.findAll()
                .flatMap(blockedToken -> {
                    try {
                        JWT.require(Algorithm.HMAC256(securityProperties.getSalt()))
                                .build()
                                .verify(blockedToken.getId());
                        return Mono.empty();
                    } catch (JWTVerificationException e) {
                        return blockedTokenRepository.delete(blockedToken);
                    }
                });
    }
}
