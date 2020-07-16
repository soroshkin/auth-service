package com.icl.auth.repository;

import com.icl.auth.exception.TokenNotFoundException;
import com.icl.auth.model.ExpiredToken;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class ReactiveExpiredTokenRepository implements TokenRepository {
    private ConnectionFactory connectionFactory;

    @Autowired
    public ReactiveExpiredTokenRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Inserts expired token into table
     *
     * @param token to be saved
     * @return saved expired token
     */
    @Override
    public Mono<ExpiredToken> create(ExpiredToken token) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(Mono.from(connection.createStatement("INSERT INTO EXPIRED_TOKEN (id) VALUES ($1)")
                                .bind("$1", token.getId())
                                .returnGeneratedValues("id")
                                .execute())
                                .doFinally(signalType -> close(connection)))
                        .map(result ->
                                result.map((row, rowMetadata) ->
                                        new ExpiredToken(row.get(0, String.class))))
                        .flatMap(Mono::from)
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(st -> close(connection)));
    }

    /**
     * Deletes token from database
     *
     * @param token to be deleted
     */
    @Override
    public Mono<Object> delete(ExpiredToken token) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(findById(token.getId()))
                        .then(Mono.from(connection.createStatement("DELETE FROM EXPIRED_TOKEN WHERE id=$1")
                                .bind("$1", token.getId())
                                .execute()))
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(signalType -> close(connection)));
    }

    /**
     * Searches token by id
     *
     * @param id of the token
     * @return found token or Mono.error with {@link TokenNotFoundException} exception
     */
    @Override
    public Mono<ExpiredToken> findById(String id) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT id FROM EXPIRED_TOKEN WHERE id=$1")
                                .bind("$1", id)
                                .execute())
                                .doFinally(signalType -> close(connection)))
                .map(result ->
                        result.map((row, rowMetadata) ->
                                new ExpiredToken(row.get(0, String.class))))
                .flatMap(Mono::from)
                .switchIfEmpty(Mono.error(TokenNotFoundException::new));
    }

    private <T> Mono<T> close(Connection connection) {
        return Mono.from(connection.close())
                .then(Mono.empty());
    }
}
