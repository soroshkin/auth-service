package com.icl.auth.repository;

import com.icl.auth.exception.UserNotFoundException;
import com.icl.auth.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository {
    Mono<User> findById(Long id);

    Mono<User> findByLogin(String login);

    Mono<User> create(User user);

    Mono<User> update(User user);

    Mono<Object> deleteById(Long id) throws UserNotFoundException;

    Flux<User> findAll();
}
