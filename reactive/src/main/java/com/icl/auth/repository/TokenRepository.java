package com.icl.auth.repository;

import com.icl.auth.model.BlockedToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenRepository {
    Mono<BlockedToken> create(BlockedToken token);

    Mono<BlockedToken> delete(BlockedToken token);

    Mono<BlockedToken> findById(String id);

    Flux<BlockedToken> findAll();
}
