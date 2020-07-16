package com.icl.auth.repository;

import com.icl.auth.model.ExpiredToken;
import reactor.core.publisher.Mono;

public interface TokenRepository {
    Mono<ExpiredToken> create(ExpiredToken token);

    Mono<Object> delete(ExpiredToken token);

    Mono<ExpiredToken> findById(String id);
}
