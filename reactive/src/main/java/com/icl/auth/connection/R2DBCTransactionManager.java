package com.icl.auth.connection;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class R2DBCTransactionManager<T> implements TransactionManager<T, Connection> {
    private ConnectionFactory connectionFactory;

    @Autowired
    public R2DBCTransactionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Method wraps CRUD operations into transaction
     *
     * @param function CRUD operation to be processed
     * @return {@link Mono} of BlockedToken, which is the result of operation
     */
    @Override
    public Mono<T>
    executeInTransaction(Function<Connection, Mono<T>> function) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(function.apply(connection))
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(st -> close(connection).subscribe()));
    }

    @Override
    public Mono<T>
    executeWithoutTransaction(Function<Connection, Mono<T>> function) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(function.apply(connection))
                        .doFinally(st -> close(connection).subscribe()));
    }

    @Override
    public Mono<Void> close(Connection connection) {
        return Mono.from(connection.close());
    }
}
