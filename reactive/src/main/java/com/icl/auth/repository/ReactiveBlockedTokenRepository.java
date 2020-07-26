package com.icl.auth.repository;

import com.icl.auth.exception.TokenNotFoundException;
import com.icl.auth.model.BlockedToken;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Repository
public class ReactiveBlockedTokenRepository implements TokenRepository {
    private ConnectionFactory connectionFactory;

    @Autowired
    public ReactiveBlockedTokenRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Inserts expired token into table
     *
     * @param token to be saved
     * @return saved expired token
     */
    @Override
    public Mono<BlockedToken> create(BlockedToken token) {
        return executeInTransaction(connection ->
                Mono.from(connection.createStatement("INSERT INTO BLOCKED_TOKEN (id) VALUES ($1)")
                        .bind("$1", token.getId())
                        .returnGeneratedValues("id")
                        .execute())
                        .map(result ->
                                result.map((row, rowMetadata) ->
                                        new BlockedToken(row.get(0, String.class))))
                        .flatMap(Mono::from));
    }

    /**
     * Deletes token from database
     *
     * @param token to be deleted
     */
    @Override
    public Mono<BlockedToken> delete(BlockedToken token) {
        return executeInTransaction(connection ->
                Mono.from(Mono.from(connection.createStatement("DELETE FROM BLOCKED_TOKEN WHERE id=$1")
                        .bind("$1", token.getId())
                        .execute()))
                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        .flatMap(count -> {
                            if (count == 0) {
                                return Mono.error(TokenNotFoundException::new);
                            } else {
                                return Mono.just(token);
                            }
                        }));
    }

    /**
     * Finds all blocked tokens in BLOCKED_TOKEN table
     *
     * @return {@link Flux} of BlockedToken
     */
    @Override
    public Flux<BlockedToken> findAll() {
        return Flux.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT id FROM BLOCKED_TOKEN")
                                .execute())
                                .doFinally(signalType -> close(connection)))
                .map(result ->
                        result.map((row, rowMetadata) ->
                                new BlockedToken(row.get(0, String.class))))
                .flatMap(Mono::from)
                .switchIfEmpty(Mono.error(TokenNotFoundException::new));

    }

    /**
     * Searches token by id
     *
     * @param id of the token
     * @return found token or Mono.error with {@link TokenNotFoundException} exception
     */
    @Override
    public Mono<BlockedToken> findById(String id) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT id FROM BLOCKED_TOKEN WHERE id=$1")
                                .bind("$1", id)
                                .execute())
                                .doFinally(signalType -> close(connection)))
                .map(result ->
                        result.map((row, rowMetadata) ->
                                new BlockedToken(row.get(0, String.class))))
                .flatMap(Mono::from)
                .switchIfEmpty(Mono.error(TokenNotFoundException::new));
    }

    /**
     * Method wraps CRUD operations into transaction
     *
     * @param function CRUD operation to be processed
     * @return {@link Mono} of BlockedToken, which is the result of operation
     */
    protected Mono<BlockedToken>
    executeInTransaction(Function<Connection, Mono<BlockedToken>> function) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(function.apply(connection))
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(st -> close(connection).subscribe()));
    }

    private Mono<Void> close(Connection connection) {
        return Mono.from(connection.close());
    }
}
