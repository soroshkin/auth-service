package com.icl.auth.connection;

import io.r2dbc.spi.Connection;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Interface, which provides connections to database. Can execute functions inside or without transaction.
 *
 * @param <T> object type got from database
 * @param <C> connection, which is used to connect to database
 */
public interface TransactionManager<T, C> {
    Mono<T> executeInTransaction(Function<C, Mono<T>> function);

    Mono<T> executeWithoutTransaction(Function<C, Mono<T>> function);

    Mono<Void> close(Connection connection);
}
