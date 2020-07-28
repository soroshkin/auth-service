package com.icl.auth.connection;

import com.icl.auth.model.BlockedToken;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ValidationDepth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

@SpringBootTest
class R2DBCTransactionManagerTest {

    @Autowired
    private R2DBCTransactionManager<BlockedToken> transactionManager;

    @Test
    void executeInTransaction() {
        Connection[] connection = new Connection[1];

        StepVerifier.create(transactionManager.executeInTransaction(conn -> {
            connection[0] = conn;
            return Mono.just(new BlockedToken("new token"));
        })
                .map(blockedToken -> Mono.from(connection[0].validate(ValidationDepth.LOCAL)))
                .map(Mono::from))
                .expectNextMatches(booleanMono ->
                        Objects.requireNonNull(booleanMono.map(Boolean::booleanValue).block()))
                .verifyComplete();

        StepVerifier.create(connection[0].validate(ValidationDepth.LOCAL))
                .expectNext(false)
                .verifyComplete();
    }
}