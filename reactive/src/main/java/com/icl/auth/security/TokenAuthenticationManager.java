package com.icl.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.icl.auth.exception.TokenNotFoundException;
import com.icl.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TokenAuthenticationManager {
    private TokenService tokenService;

    @Autowired
    public TokenAuthenticationManager(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Method checks if given token is expired or illegal.
     *
     * @param token to be checked
     * @return {@link Mono} of String, which represents token or Mono.error, if token is expired or not legal
     */
    public Mono<String> authorize(String token) {
        return tokenService.findExpiredTokenById(token)
                .flatMap(expiredToken -> {
                    if (expiredToken != null) {
                        return Mono.error(new TokenExpiredException(expiredToken.toString()));
                    } else {
                        return Mono.just(token);
                    }
                })
                .switchIfEmpty(Mono.error(TokenNotFoundException::new))
                .onErrorResume(error -> {
                    if (error instanceof TokenNotFoundException) {
                        return Mono.just(
                                JWT.require(Algorithm.HMAC256("aM1?@30Oi_"))
                                        .build()
                                        .verify(token).getToken());
                    }
                    return Mono.error(new TokenExpiredException("token expired"));
                });
    }
}
