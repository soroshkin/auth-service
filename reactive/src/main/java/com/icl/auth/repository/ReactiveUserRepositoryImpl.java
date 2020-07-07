package com.icl.auth.repository;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.model.User;
import com.icl.auth.security.Role;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public class ReactiveUserRepositoryImpl implements ReactiveUserRepository {
    private ConnectionFactory connectionFactory;

    @Autowired
    public ReactiveUserRepositoryImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Mono<User> findById(Long id) {
        return findByCriteria(id, "SELECT id, login, password, date_of_birth, role FROM User WHERE id=$1");
    }

    @Override
    public Mono<User> findByLogin(String login) {
        return findByCriteria(login, "SELECT id, login, password, date_of_birth, role FROM User WHERE login=$1");
    }

    @Override
    public Mono<User> create(User user) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(Mono.from(connection.createStatement(
                                "insert into user (date_of_birth, login, password, role) values ($1,$2,$3,$4)")
                                .bind("$1", user.getDateOfBirth())
                                .bind("$2", user.getLogin())
                                .bind("$3", user.getPassword())
                                .bind("$4", user.getRole().toString())
                                .returnGeneratedValues("id")
                                .execute()))
                        .map(result -> result.map((row, rowMetaData) ->
                                new User(row.get("id", Long.class),
                                        user.getLogin(),
                                        user.getPassword(),
                                        user.getDateOfBirth(),
                                        user.getRole())))
                        .flatMap(Mono::from)
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(st -> close(connection))
                );
    }

    @Override
    public Mono<User> update(User user) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(Mono.from(connection.createStatement(
                                "UPDATE user set date_of_birth=$1, login=$2, password=$3, role=$4 where id=$5")
                                .bind("$1", user.getDateOfBirth())
                                .bind("$2", user.getLogin())
                                .bind("$3", user.getPassword())
                                .bind("$4", user.getRole().toString())
                                .bind("$5", user.getId())
                                .execute()))
                        .then(Mono.from(connection.createStatement(
                                "SELECT id, login, password, date_of_birth, role FROM User WHERE id=$1")
                                .bind("$1", user.getId())
                                .execute()))
                        .map(result -> result.map((row, rowMetadata) ->
                                new User(user.getId(),
                                        user.getLogin(),
                                        user.getPassword(),
                                        user.getDateOfBirth(),
                                        user.getRole())))
                        .flatMap(Mono::from)
                        .switchIfEmpty(Mono.error(UserNotFoundException::new))
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(st -> close(connection)));
    }

    @Override
    public Mono<Object> deleteById(Long id) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.beginTransaction())
                        .then(findById(id))
                        .then(Mono.from(connection.createStatement("delete from user where id=$1")
                                .bind("$1", id)
                                .execute()))
                        .delayUntil(r -> connection.commitTransaction())
                        .doFinally(signalType -> close(connection)));
    }

    @Override
    public Flux<User> findAll() {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT id, login, password, date_of_birth, role  FROM user")
                                .execute())
                                .doFinally(st -> close(connection)))
                .flatMapMany(result -> Flux.from(result.map((row, rowMetadata) ->
                        new User(row.get("id", Long.class),
                                row.get("login", String.class),
                                row.get("password", String.class),
                                row.get("date_of_birth", LocalDate.class),
                                Role.valueOf(row.get("role", String.class))
                        ))));
    }

    private <T> Mono<T> close(Connection connection) {
        return Mono.from(connection.close())
                .then(Mono.empty());
    }

    private <T> Mono<User> findByCriteria(T searchCriteria, String sql) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection -> Mono.from(connection.createStatement(
                        sql)
                        .bind("$1", searchCriteria)
                        .execute())
                        .doFinally(signalType -> close(connection))
                        .map(result ->
                                result.map((row, rowMetadata) ->
                                        new User(row.get("id", Long.class),
                                                row.get("login", String.class),
                                                row.get("password", String.class),
                                                row.get("date_of_birth", LocalDate.class),
                                                Role.valueOf(row.get("role", String.class)))))
                        .flatMap(Mono::from)
                        .switchIfEmpty(Mono.error(UserNotFoundException::new)));
    }
}
